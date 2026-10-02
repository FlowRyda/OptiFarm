package at.ac.tuwien.ase.groupphase.backend.service.impl.scheduling;

import at.ac.tuwien.ase.groupphase.backend.entity.ActiveField;
import at.ac.tuwien.ase.groupphase.backend.entity.Field;
import at.ac.tuwien.ase.groupphase.backend.entity.Task;
import at.ac.tuwien.ase.groupphase.backend.repository.ActiveFieldRepository;
import at.ac.tuwien.ase.groupphase.backend.service.ForecastService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Calculates a baseValue for each field that is present in a task.
 *
 * <p>
 * BaseValue will be calculated based on profit/ha if an active field exists.
 * If not, a forecast for this field will be inserted, based on the last year.
 * If this forecast value does not exist, field area in ha will determine the baseValue.
 */
public class PriorityContext {
    private static final double LOW_P = 0.05;
    private static final double HIGH_P = 0.95;

    public PriorityContextData buildPriorityContext(
        List<Task> tasks,
        ActiveFieldRepository activeFieldRepository,
        ForecastService forecastService
    ) {
        // Collect unique fields referenced by tasks
        Map<Long, Field> fieldById = new HashMap<>();
        for (Task t : tasks) {
            Field f = t.getField();
            if (f != null && f.getId() != null) {
                fieldById.putIfAbsent(f.getId(), f);
            }
        }

        Map<Long, ActiveField> activeFieldByFieldId = new HashMap<>();
        Map<Long, Double> baseValueByFieldId = new HashMap<>();

        Map<Long, Double> rawProfitPerHaByFieldId = new HashMap<>();
        Map<Long, Double> forecastNormByFieldId = new HashMap<>();
        double minArea = Double.POSITIVE_INFINITY;
        double maxArea = Double.NEGATIVE_INFINITY;

        for (Map.Entry<Long, Field> entry : fieldById.entrySet()) {
            Long fieldId = entry.getKey();
            Field field = entry.getValue();


            ActiveField af = fetchActiveField(activeFieldRepository, fieldId);
            if (af != null) {
                activeFieldByFieldId.put(fieldId, af);
            }

            double areaHa = field.getArea();
            minArea = Math.min(minArea, areaHa);
            maxArea = Math.max(maxArea, areaHa);

            if (af != null) {
                // ActiveField exists -> compute profit/ha
                if (af.getSeed() != null) {
                    double buying = af.getSeed().getBuyingPrice();
                    double selling = af.getSellingPrice();
                    double profitPerField = (selling - buying) * areaHa;

                    // negative profit baselines at 0
                    if (profitPerField < 0) {
                        profitPerField = 0;
                    }

                    if (Double.isFinite(profitPerField)) {
                        rawProfitPerHaByFieldId.put(fieldId, profitPerField);
                    }
                }
            } else {
                // No ActiveField -> use forecast, if not present -1 is returned
                double forecast = forecastService.calculateNormalizedForecast(fieldId);
                if (forecast >= 0.0) {
                    forecastNormByFieldId.put(fieldId, clamp01(forecast));
                }
            }
        }

        // Prepare profit normalization bounds (winsorized min/max)
        ProfitNormBounds profitBounds = computeWinsorizedBounds(rawProfitPerHaByFieldId.values());

        // Compute normalized profit (0..1) for fields with profit
        Map<Long, Double> profitNormByFieldId = new HashMap<>();
        for (Map.Entry<Long, Double> e : rawProfitPerHaByFieldId.entrySet()) {
            double norm = normalizeWithBounds(e.getValue(), profitBounds);
            profitNormByFieldId.put(e.getKey(), norm);
        }

        // Reference level R in [0..1] used to scale area fallback so it’s comparable.
        // Use median of known normalized values (profit + forecast). If none exist, default to 0.5.
        double referenceR = medianOrDefault(
            concatValues(profitNormByFieldId.values(), forecastNormByFieldId.values()),
            0.5
        );

        // Second pass: assign final baseNormalized for each field, then store scaled long
        for (Long fieldId : fieldById.keySet()) {
            Field field = fieldById.get(fieldId);
            boolean hasActiveField = activeFieldByFieldId.containsKey(fieldId);

            double baseNorm;

            if (hasActiveField) {
                // ActiveField => use normalized profit if present, else 0
                baseNorm = profitNormByFieldId.getOrDefault(fieldId, 0.0);
            } else {
                // No ActiveField => forecast if present else area fallback
                Double forecast = forecastNormByFieldId.get(fieldId);
                if (forecast != null) {
                    baseNorm = forecast;
                } else {
                    // Area fallback: normalize area -> [0..1], soften with sqrt,
                    // then scale by referenceR so area fallback is “typical” compared to profit/forecast.
                    double areaHa = field.getArea();
                    double areaNorm = normalizeMinMax(areaHa, minArea, maxArea);
                    areaNorm = 0.2 + 0.8 * Math.sqrt(areaNorm);

                    // conservative value because baseNorm cannot exceed referenceR
                    baseNorm = clamp01(referenceR * areaNorm);
                }
            }

            baseValueByFieldId.put(fieldId, clamp01(baseNorm));
        }

        return new PriorityContextData(activeFieldByFieldId, baseValueByFieldId);
    }

    private double clamp01(double v) {
        if (v < 0) {
            return 0;
        }
        if (v > 1) {
            return 1;
        }
        return v;
    }

    private double normalizeMinMax(double v, double min, double max) {
        if (!Double.isFinite(v) || !Double.isFinite(min) || !Double.isFinite(max)) {
            return 0.0;
        }
        if (max <= min) {
            return 0.5; // all same area => neutral
        }
        double x = (v - min) / (max - min);
        return clamp01(x);
    }

    private ActiveField fetchActiveField(ActiveFieldRepository repo, Long fieldId) {
        return repo.findByFieldIdAndEndDateIsNull(fieldId).orElse(null);
    }

    private static final class ProfitNormBounds {
        final double low;
        final double high;

        ProfitNormBounds(double low, double high) {
            this.low = low;
            this.high = high;
        }
    }

    private ProfitNormBounds computeWinsorizedBounds(Collection<Double> rawValues) {
        List<Double> vals = rawValues.stream()
            .filter(Objects::nonNull)
            .filter(Double::isFinite)
            .sorted()
            .collect(Collectors.toList());

        if (vals.isEmpty()) {
            // no profit data; bounds don’t matter because profitNorm map will be empty
            return new ProfitNormBounds(0.0, 1.0);
        }

        double low = percentile(vals, LOW_P);
        double high = percentile(vals, HIGH_P);

        // If everything collapses to a single value, avoid division by zero later
        if (high <= low) {
            return new ProfitNormBounds(low, low);
        }
        return new ProfitNormBounds(low, high);
    }

    private double normalizeWithBounds(double v, ProfitNormBounds b) {
        if (!Double.isFinite(v)) {
            return 0.0;
        }

        double x = v;
        if (x < b.low) {
            x = b.low;
        }
        if (x > b.high) {
            x = b.high;
        }

        if (b.high <= b.low) {
            return 0.5; // baseline at 0.5 when all values are equal
        }

        return clamp01((x - b.low) / (b.high - b.low));
    }

    private double percentile(List<Double> sortedVals, double p) {
        if (sortedVals.isEmpty()) {
            return 0.0;
        }
        if (p <= 0) {
            return sortedVals.get(0);
        }
        if (p >= 1) {
            return sortedVals.get(sortedVals.size() - 1);
        }

        int n = sortedVals.size();
        int idx = (int) Math.floor(p * (n - 1));
        return sortedVals.get(idx);
    }

    private List<Double> concatValues(Collection<Double> a, Collection<Double> b) {
        ArrayList<Double> out = new ArrayList<>(a.size() + b.size());
        out.addAll(a);
        out.addAll(b);
        return out;
    }

    private double medianOrDefault(List<Double> values, double defaultVal) {
        List<Double> v = values.stream()
            .filter(Objects::nonNull)
            .filter(Double::isFinite)
            .sorted()
            .toList();

        if (v.isEmpty()) {
            return defaultVal;
        }
        int n = v.size();
        if (n % 2 == 1) {
            return v.get(n / 2);
        }
        return (v.get(n / 2 - 1) + v.get(n / 2)) / 2.0;
    }

    /**
     * Data structure which has 2 maps, one for active fields and one for the baseValue, query by fieldId.
     */
    public static final class PriorityContextData {
        private final Map<Long, ActiveField> activeFieldByFieldId;
        private final Map<Long, Double> baseValueByFieldId;

        public PriorityContextData(
            Map<Long, ActiveField> activeFieldByFieldId,
            Map<Long, Double> baseValueByFieldId
        ) {
            this.activeFieldByFieldId = Map.copyOf(activeFieldByFieldId);
            this.baseValueByFieldId = Map.copyOf(baseValueByFieldId);
        }

        /**
         * Returns the active field of the field with fieldId.
         */
        public ActiveField activeFieldForFieldId(Long fieldId) {
            return activeFieldByFieldId.get(fieldId);
        }

        /**
         * Returns boolean value if field with fieldId has an active field.
         */
        public boolean hasActiveField(Long fieldId) {
            return activeFieldByFieldId.containsKey(fieldId);
        }

        /**
         * Base value scaled to normalized [0..1].
         * Returns 0, if no value present.
         */
        public double baseValueForFieldId(long fieldId) {
            return baseValueByFieldId.getOrDefault(fieldId, 0.0);
        }
    }
}