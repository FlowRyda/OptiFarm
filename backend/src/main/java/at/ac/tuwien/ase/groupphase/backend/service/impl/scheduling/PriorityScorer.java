package at.ac.tuwien.ase.groupphase.backend.service.impl.scheduling;

import at.ac.tuwien.ase.groupphase.backend.entity.ActiveField;
import at.ac.tuwien.ase.groupphase.backend.entity.Field;
import at.ac.tuwien.ase.groupphase.backend.entity.Task;
import at.ac.tuwien.ase.groupphase.backend.entity.TaskType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Returns a single numeric priority score for a specific task.
 *
 * <p>
 * Priority score is based on a baseValue calculated in PriorityContext.
 * Then TaskCategory and fieldProgress are taken into account,
 * anti-starvation Bonuses, urgency multipliers are applied to
 * calculate a score between 0 and roughly 9 000 000.
 */
public class PriorityScorer {
    // --- Type bonuses (anti-starvation / ordering) ---
    // todo Tune these after logging a few scores.
    private static final Logger log = LoggerFactory.getLogger(PriorityScorer.class);
    private static final long EMERGENCY_BONUS = 4_000_000L;
    private static final long HARVEST_BONUS = 3_000_000L;
    private static final long PLANT_BONUS = 2_000_000L;

    // --- Scales for business components (so values sit in similar ranges) ---
    private static final double HARVEST_SCALE = 1_200_000.0;
    private static final double PLANT_SCALE = 300_000.0;
    private static final double MAINT_SCALE = 1_000_000.0;
    private static final double NONE_SCALE = 100_000.0;

    public long score(Task task, PriorityContext.PriorityContextData ctx) {
        if (task == null) {
            return 0L;
        }

        Field field = task.getField();
        if (field == null || field.getId() == null) {
            return 0L;
        }

        Long fieldId = field.getId();

        // base in [0..1] from your context
        double base = ctx.baseValueForFieldId(fieldId);

        // progress in [0..100] from your context
        int p = progressAtTaskTime(task, ctx, fieldId);
        double prog = clamp01(p / 100.0);

        boolean hasActiveField = ctx.hasActiveField(fieldId);

        // Urgency multiplier based on windowEnd (bounded ~ [1..2])
        double urgency = urgencyMultiplier(task.getWindowEnd(), LocalDateTime.now());

        double business = 0.0;
        long bonus = 0L;

        TaskType type = task.getTaskType();

        if (type == null) {
            business = noneScore(base, prog);
        } else {
            switch (type) {
                case TaskType.EMERGENCY -> {
                    // Emergencies should float to the top, independent of crop stage.
                    bonus = EMERGENCY_BONUS;
                    business = base * NONE_SCALE;
                }

                case TaskType.HARVESTING -> {
                    // Harvesting only makes sense on active fields
                    if (!hasActiveField) {
                        return 0L;
                    }

                    bonus = HARVEST_BONUS;
                    business = harvestScore(base, prog);
                }

                case TaskType.PLANTING -> {
                    // Planting only makes sense if field is NOT active
                    if (hasActiveField) {
                        return 0L;
                    }

                    bonus = PLANT_BONUS;
                    business = plantingScore(base);
                }

                case TaskType.MAINTENANCE -> {
                    // Maintenance is meaningful only if field is active.
                    // On non-active field -> treat as NONE baseline.
                    if (!hasActiveField) {
                        business = noneScore(base, prog);
                    } else {
                        business = maintenanceScore(base, prog);
                    }
                }

                case TaskType.NONE -> business = noneScore(base, prog);

                default -> business = noneScore(base, prog);
            }
        }

        // Apply urgency (affects both business and bonus, which is usually desirable)
        double total = (business + bonus) * urgency;

        if (!Double.isFinite(total) || total <= 0) {
            return 0L;
        }
        if (total >= Long.MAX_VALUE) {
            return Long.MAX_VALUE;
        }
        long finalScore = Math.round(total);

        String taskName = task.getName() != null ? task.getName() : "UNKNOWN_TASK";
        log.info("Task '{}' scored: {}", taskName, finalScore);

        return finalScore;
    }

    // ---------------- domain formulas ---------------- //

    /**
     * Harvest: should ramp hard as progress approaches 1.0 (late stage).
     * Using prog^3 creates a very steep ramp near harvest.
     */
    private double harvestScore(double base, double prog) {
        return base * HARVEST_SCALE * Math.pow(prog, 3);
    }

    /**
     * Planting: if field is non-active, progress is basically 0 anyway.
     * Planting priority should be driven by field value (base) and type bonus.
     */
    private double plantingScore(double base) {
        return base * PLANT_SCALE;
    }

    /**
     * Maintenance: important early, less important late.
     * We implement "early high" using inverted progress.
     *
     * <p>
     * Stage-based exponent:
     * - stage1 (<33%) -> inv^3
     * - stage2 (33..66%) -> inv^2
     * - stage3 (>=66%) -> inv^1
     */
    private double maintenanceScore(double base, double prog) {
        double inv = 1.0 - prog;

        int stage = stageFromProg(prog);
        int exp = switch (stage) {
            case 1 -> 3;
            case 2 -> 2;
            default -> 1;
        };

        return base * MAINT_SCALE * Math.pow(inv, exp);
    }

    /**
     * NONE: baseline low priority. Still uses base a bit so valuable fields drift up.
     */
    private double noneScore(double base, double prog) {
        // Slightly prefer later progress (optional); you can also drop prog completely.
        return base * NONE_SCALE * (0.5 + 0.5 * prog);
    }

    /**
     * Bounded urgency multiplier in roughly [1..2]. // todo is this multiplier even a good idea?
     * Closer windowEnd => higher urgency.
     */
    private double urgencyMultiplier(LocalDateTime windowEnd, LocalDateTime now) {
        if (windowEnd == null) {
            return 1.0;
        }

        long minutesLeft = Duration.between(now, windowEnd).toMinutes();

        if (minutesLeft <= 0) {
            return 1.0;
        }

        double hoursLeft = minutesLeft / 60.0;

        // 24h -> 1.5
        // 72h -> 1.25
        // large -> approaches 1.0
        return 1.0 + 1.0 / (1.0 + (hoursLeft / 24.0));
    }

    private int progressAtTaskTime(Task task, PriorityContext.PriorityContextData ctx, Long fieldId) {
        ActiveField af = ctx.activeFieldForFieldId(fieldId);
        if (af == null) {
            return 0;
        }

        LocalDateTime ws = task.getWindowStart();
        LocalDateTime we = task.getWindowEnd();

        LocalDateTime reference;
        if (ws != null && we != null) {
            long seconds = Duration.between(ws, we).getSeconds();
            reference = ws.plusSeconds(seconds / 2);
        } else if (we != null) {
            reference = we;
        } else {
            reference = LocalDateTime.now();
        }

        LocalDateTime start = af.getStartDate();
        LocalDateTime end = af.getExpectedHarvestDate();

        // If end/start not available, fall back to stored growthStatus
        if (start == null || end == null || !end.isAfter(start)) {
            return clampProgress(af.getGrowthStatus());
        }

        if (reference.isBefore(start)) {
            return 0;
        }
        if (!reference.isBefore(end)) {
            return 100;
        }

        double total = Duration.between(start, end).toMillis();
        double done = Duration.between(start, reference).toMillis();
        int p = (int) Math.round(100.0 * (done / total));
        return clampProgress(p);
    }

    private int clampProgress(int growthStatus) {
        if (growthStatus < 0) {
            return 0;
        }
        if (growthStatus > 100) {
            return 100;
        }
        return growthStatus;
    }

    private int stageFromProg(double prog01) {
        if (prog01 < 0.33) {
            return 1;
        }
        if (prog01 < 0.66) {
            return 2;
        }
        return 3;
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
}