package at.ac.tuwien.ase.groupphase.backend.service.validation;

import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.schedule.TaskDto;
import at.ac.tuwien.ase.groupphase.backend.exception.ValidationException;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
public class TaskValidator {

    private static final int WORK_START_HOUR = 6;
    private static final int WORK_END_HOUR = 22;
    private static final LocalTime WORK_START = LocalTime.of(WORK_START_HOUR, 0); // 06:00
    private static final LocalTime WORK_END = LocalTime.of(WORK_END_HOUR, 0);     // 22:00

    private static final double ACCEPTED_ROUNDING_ERROR = 5e-3;

    public void validateForCreate(TaskDto task) throws ValidationException {
        List<String> validationErrors = commonValidate(task);
        if (!validationErrors.isEmpty()) {
            throw new ValidationException("Validation Error", validationErrors);
        }
    }

    public void validateForUpdate(TaskDto task) throws ValidationException {
        List<String> validationErrors = commonValidate(task);
        if (!validationErrors.isEmpty()) {
            throw new ValidationException("Validation Error", validationErrors);
        }
    }

    public List<String> commonValidate(TaskDto task) {
        List<String> validationErrors = new ArrayList<>();

        if (task == null) {
            validationErrors.add("Task can not be null");
            return validationErrors;
        }

        // --- Name ---
        if (task.getName() == null || task.getName().isBlank()) {
            validationErrors.add("Name is required");
        } else if (task.getName().length() > 256) {
            validationErrors.add("Name can be at most 256 characters long");
        }

        // --- Field ---
        if (task.getField() == null || task.getField().id() == null) {
            validationErrors.add("Field is required");
        }

        // --- Work time basic check ---
        if (task.getWorkTimeEstimate() <= 0) {
            validationErrors.add("Work time estimate must be positive");
        }

        // --- Tools/Categories: at least one present ---
        if (task.getTools() == null) {
            validationErrors.add("Tools must not be null");
        }
        if (task.getCategories() == null) {
            validationErrors.add("Categories must not be null");
        }

        if (task.getTools() != null && task.getCategories() != null) {
            long toolCount = task.getTools().stream().filter(Objects::nonNull).count();
            long categoryCount = task.getCategories().stream().filter(Objects::nonNull).count();

            if (toolCount == 0 && categoryCount == 0) {
                validationErrors.add("Task must have at least one tool or at least one category");
            }
        }

        LocalDateTime windowStart = task.getWindowStart();
        LocalDateTime windowEnd = task.getWindowEnd();
        LocalDateTime assignedStart = task.getAssignedStart();
        LocalDateTime assignedEnd = task.getAssignedEnd();

        boolean hasWindowStart = windowStart != null;
        boolean hasWindowEnd = windowEnd != null;
        boolean hasAssignedStart = assignedStart != null;
        boolean hasAssignedEnd = assignedEnd != null;

        boolean anyWindow = hasWindowStart || hasWindowEnd;
        boolean anyAssigned = hasAssignedStart || hasAssignedEnd;

        // --- fixed requires assigned times ---
        if (task.isFixed()) {
            if (!hasAssignedStart || !hasAssignedEnd) {
                validationErrors.add("Fixed tasks must have an assigned Start and End defined");
            }
        } else {
            if (!hasWindowStart || !hasWindowEnd) {
                validationErrors.add("Non-fixed tasks must have windowStart and windowEnd defined");
            }
        }

        // --- Window range validation ---
        if (anyWindow) {
            if (!hasWindowStart || !hasWindowEnd) {
                validationErrors.add("Both window start and window end are required when using a window time range");
            } else {
                validateWindowTimeRange(windowStart, windowEnd, task.getWorkTimeEstimate(), validationErrors);
            }
        }

        // --- Assigned range validation ---
        if (anyAssigned) {
            if (!hasAssignedStart || !hasAssignedEnd) {
                validationErrors.add("Both assigned start and assigned end are required when using an assigned time range");
            } else {
                if (!assignedEnd.isAfter(assignedStart)) {
                    validationErrors.add("Assigned end must be after assigned start");
                } else {
                    // start must be within [06:00, 22:00)
                    if (!isValidWorkStart(assignedStart)) {
                        validationErrors.add("Assigned start must be within working hours [06:00-22:00)");
                    }

                    // end must be within (06:00, 22:00]
                    if (!isValidWorkEnd(assignedEnd)) {
                        validationErrors.add("Assigned end must be within working hours (06:00-22:00]");
                    }

                    // Work time estimate must fit into the assigned time range (working-hours aware)
                    if (task.getWorkTimeEstimate() > 0) {
                        double availableHours = calculateAvailableWorkingHours(assignedStart, assignedEnd);

                        if (task.getWorkTimeEstimate() > availableHours + ACCEPTED_ROUNDING_ERROR) {
                            validationErrors.add(String.format(
                                "Work time estimate (%.2fh) exceeds available working hours (%.2fh) in assigned range (06:00-22:00 daily)",
                                task.getWorkTimeEstimate(), availableHours
                            ));
                        }
                    }
                }
            }
        }
        return validationErrors;
    }

    private void validateWindowTimeRange(LocalDateTime windowStart,
                                         LocalDateTime windowEnd,
                                         float workTimeEstimate,
                                         List<String> errors) {
        if (windowStart.isAfter(windowEnd)) {
            errors.add("Window end in task must be after window start");
            return;
        }

        double availableHours = calculateAvailableWorkingHours(windowStart, windowEnd);

        if (workTimeEstimate > availableHours + ACCEPTED_ROUNDING_ERROR) {
            errors.add(String.format(
                "Work time estimate (%.2fh) exceeds available working hours (%.2fh) in window (06:00-22:00 daily)",
                workTimeEstimate, availableHours
            ));
        }
    }

    /**
     * Calculates the amount of working time contained in [start, end],
     * where working time is daily between 06:00 and 22:00.
     * This naturally supports ranges that span multiple days and/or cross the night.
     */
    private double calculateAvailableWorkingHours(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null || !end.isAfter(start)) {
            return 0.0;
        }

        double totalMinutes = 0.0;
        LocalDate currentDate = start.toLocalDate();
        LocalDate endDate = end.toLocalDate();

        while (!currentDate.isAfter(endDate)) {
            LocalDateTime dayWorkStart = LocalDateTime.of(currentDate, WORK_START);
            LocalDateTime dayWorkEnd = LocalDateTime.of(currentDate, WORK_END);

            LocalDateTime effectiveStart = start.isAfter(dayWorkStart) ? start : dayWorkStart;
            LocalDateTime effectiveEnd = end.isBefore(dayWorkEnd) ? end : dayWorkEnd;

            if (effectiveStart.isBefore(effectiveEnd)) {
                totalMinutes += Duration.between(effectiveStart, effectiveEnd).toMinutes();
            }

            currentDate = currentDate.plusDays(1);
        }

        return totalMinutes / 60.0;
    }

    /**
     * Start must be within [06:00, 22:00).
     * - 06:00 allowed
     * - 21:59 allowed
     * - 22:00 NOT allowed as start
     */
    private boolean isValidWorkStart(LocalDateTime dt) {
        LocalTime t = dt.toLocalTime();
        return !t.isBefore(WORK_START) && t.isBefore(WORK_END);
    }

    /**
     * End must be within (06:00, 22:00].
     * - 22:00 allowed as end
     * - 06:00 NOT allowed as end
     */
    private boolean isValidWorkEnd(LocalDateTime dt) {
        LocalTime t = dt.toLocalTime();
        return t.isAfter(WORK_START) && !t.isAfter(WORK_END);
    }
}