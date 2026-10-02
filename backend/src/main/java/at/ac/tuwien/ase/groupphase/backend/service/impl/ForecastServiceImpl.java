package at.ac.tuwien.ase.groupphase.backend.service.impl;

import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.visualization.ForecastDto;
import at.ac.tuwien.ase.groupphase.backend.entity.Crop;
import at.ac.tuwien.ase.groupphase.backend.entity.Farmer;
import at.ac.tuwien.ase.groupphase.backend.entity.Field;
import at.ac.tuwien.ase.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.ase.groupphase.backend.repository.CropRepository;
import at.ac.tuwien.ase.groupphase.backend.repository.FarmerRepository;
import at.ac.tuwien.ase.groupphase.backend.repository.FieldRepository;
import at.ac.tuwien.ase.groupphase.backend.security.JwtUtil;
import at.ac.tuwien.ase.groupphase.backend.service.ForecastService;
import com.github.signaflo.timeseries.TimeSeries;
import com.github.signaflo.timeseries.forecast.Forecast;
import com.github.signaflo.timeseries.model.arima.Arima;
import com.github.signaflo.timeseries.model.arima.ArimaOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Calendar;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;

@Service
public class ForecastServiceImpl implements ForecastService {

    private static final Logger logger = LoggerFactory.getLogger(ForecastServiceImpl.class);
    private final FarmerRepository farmerRepository;
    private final CropRepository cropRepository;
    private final FieldRepository fieldRepository;

    public ForecastServiceImpl(FarmerRepository farmerRepository, FieldRepository fieldRepository,
                               CropRepository cropRepository) {
        this.farmerRepository = farmerRepository;
        this.cropRepository = cropRepository;
        this.fieldRepository = fieldRepository;
    }

    public Farmer getLoggedInFarmer() {
        var user = JwtUtil.getActiveJwtUser();
        return farmerRepository.findById(user.id()).orElseThrow(
            () -> new NotFoundException("Farmer not found: " + user.id())
        );
    }

    public static ArimaOrder findOptimalArimaOrder(TimeSeries series) {
        final int max_autoRegressiveOrder = 3;
        final int max_movingAverageOrder = 3;
        final int max_degreeOfDifferencing = 2;

        double bestAic = Double.MAX_VALUE;
        ArimaOrder bestOrder = null;

        for (int degreeOfDifferencing = 0; degreeOfDifferencing <= max_degreeOfDifferencing; degreeOfDifferencing++) {
            for (int autoRegressiveOrder = 0; autoRegressiveOrder <= max_autoRegressiveOrder; autoRegressiveOrder++) {
                for (int movingAverageOrder = 0; movingAverageOrder <= max_movingAverageOrder; movingAverageOrder++) {

                    if (autoRegressiveOrder == 0 && degreeOfDifferencing == 0 && movingAverageOrder == 0) {
                        continue;
                    }

                    try {
                        ArimaOrder order = ArimaOrder.order(autoRegressiveOrder, degreeOfDifferencing, movingAverageOrder);

                        Arima model = Arima.model(series, order);
                        double aic = model.aic();

                        if (aic < bestAic) {
                            bestAic = aic;
                            bestOrder = order;
                        }

                    } catch (Exception ex) {
                        logger.debug("ARIMA({}, {}, {}) unstable or non-convergent: {}",
                            autoRegressiveOrder, degreeOfDifferencing, movingAverageOrder, ex.getMessage());
                    }
                }
            }
        }

        if (bestOrder == null) {
            if (series.size() < 3) {
                return ArimaOrder.order(1, 0, 0);
            }
            return ArimaOrder.order(0, 1, 1);
        }
        return bestOrder;
    }

    public static ForecastDto calculateForecast(List<Crop> crops, int forecastLength, Long id) {
        if (crops.isEmpty()) {
            return new ForecastDto(id, Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap());
        }

        Map<Integer, Double> cropYieldsTimeseries = calculateCropYieldsTimeseries(crops);
        int maxKey = Collections.max(cropYieldsTimeseries.keySet());
        double[] data = cropYieldsTimeseries.values().stream().mapToDouble(Double::doubleValue).toArray();

        TimeSeries series = TimeSeries.from(data);
        ArimaOrder order = ForecastServiceImpl.findOptimalArimaOrder(series);
        logger.info("Optimal ARIMA order for field {}", order);
        Arima model = Arima.model(series, order);
        Forecast forecast = model.forecast(forecastLength);

        double[] pointForecast = forecast.pointEstimates().asArray();
        double[] lower = forecast.lowerPredictionInterval().asArray();
        double[] upper = forecast.upperPredictionInterval().asArray();

        Map<Integer, Double> forecastTimeSeries = new HashMap<>();
        Map<Integer, Double> lowerPredictionIntervalTimeSeries = new HashMap<>();
        Map<Integer, Double> upperPredictionIntervalTimeSeries = new HashMap<>();

        for (int i = 0; i < forecastLength; i++) {
            forecastTimeSeries.put(maxKey + (i + 1), pointForecast[i]);
            lowerPredictionIntervalTimeSeries.put(maxKey + (i + 1), lower[i]);
            upperPredictionIntervalTimeSeries.put(maxKey + (i + 1), upper[i]);
        }

        ForecastDto forecastDto = new ForecastDto(id, forecastTimeSeries,
            lowerPredictionIntervalTimeSeries, upperPredictionIntervalTimeSeries);
        return forecastDto;
    }

    public static Map<Integer, Double> calculateCropYieldsTimeseries(List<Crop> crops) {
        return crops.stream()
            .collect(Collectors.groupingBy(
                crop -> crop.getHarvestDate().getYear(),
                Collectors.summingDouble(crop -> crop.getCurrentPrice() * crop.getQuantity())
            ));
    }

    @Override
    public double calculateNormalizedForecast(Long fieldId) {
        List<Field> fields = fieldRepository.findAllByOwner(this.getLoggedInFarmer());
        Set<Double> fieldForecasts = new HashSet<>();
        double valueTobeNormalized = 0;
        double forecastValue = 0;

        for (Field field : fields) {
            List<Crop> crops = cropRepository.findByFieldAndOwner(field, this.getLoggedInFarmer());
            if (!crops.isEmpty()) {
                crops = removeCropsOfUnfinishedYear(crops);
                forecastValue = calculateForecast(crops, 1, field.getId()).getForecastTimeSeries().values().stream().findFirst().get();
                fieldForecasts.add(forecastValue);
                if (field.getId().equals(fieldId)) {
                    valueTobeNormalized = forecastValue;
                }

            }
        }

        if (fieldForecasts.isEmpty() || valueTobeNormalized == 0) {
            return -1;
        }

        double maxForecast = Collections.max(fieldForecasts);
        double minForecast = Collections.min(fieldForecasts);

        if (maxForecast == minForecast) {
            return -1;
        }

        return ((valueTobeNormalized - minForecast) / (maxForecast - minForecast));
    }

    private List<Crop> removeCropsOfUnfinishedYear(List<Crop> crops) {
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        return crops.stream()
            .filter(crop -> crop.getHarvestDate().getYear() < currentYear)
            .collect(Collectors.toList());
    }

}
