package at.ac.tuwien.ase.groupphase.backend.service.validation;

import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.schedule.PlannedTaskUpdateDto;
import at.ac.tuwien.ase.groupphase.backend.exception.ValidationException;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class PlannedTaskValidator {

    private static final int WORK_START_HOUR = 6;
    private static final int WORK_END_HOUR = 22;
    private static final double ACCEPTED_ROUNDING_ERROR = 5e-3;

    public void validateUpdateObject(PlannedTaskUpdateDto dto) throws ValidationException {
        List<String> validationErrors = new ArrayList<>();

        if (dto == null) {
            throw new ValidationException("Validation Error", List.of("DTO cannot be null"));
        }

        // --- Work time basic check ---
        if (dto.getWorkTimeEstimate() <= 0) {
            validationErrors.add("Work time estimate must be positive");
        }

        // --- Extract values for convenience ---
        LocalDateTime windowStart = dto.getWindowStart();
        LocalDateTime windowEnd = dto.getWindowEnd();
        LocalDateTime assignedStart = dto.getAssignedStart();
        LocalDateTime assignedEnd = dto.getAssignedEnd();

        boolean hasWindowStart = windowStart != null;
        boolean hasWindowEnd = windowEnd != null;
        boolean hasAssignedStart = assignedStart != null;
        boolean hasAssignedEnd = assignedEnd != null;

        boolean anyWindow = hasWindowStart || hasWindowEnd;
        boolean anyAssigned = hasAssignedStart || hasAssignedEnd;

        // --- Fixed requires assigned times ---
        if (dto.isFixed()) {
            if (!hasAssignedStart || !hasAssignedEnd) {
                validationErrors.add("Fixed tasks must have an assigned Start and End defined");
            }
        }

        // --- Window range validation (work-hours aware) ---
        if (anyWindow) {
            if (!hasWindowStart || !hasWindowEnd) {
                validationErrors.add("Both window start and window end must be provided together");
            } else {
                validateWindowTimeRange(windowStart, windowEnd, dto.getWorkTimeEstimate(), validationErrors);
            }
        }

        // --- Assigned range validation (work-hours aware) ---
        if (anyAssigned) {
            if (!hasAssignedStart || !hasAssignedEnd) {
                validationErrors.add("Both assigned start and assigned end must be provided together");
            } else {
                if (!assignedEnd.isAfter(assignedStart)) {
                    validationErrors.add("Assigned end must be after assigned start");
                } else {
                    // Start/end cannot be in night time
                    if (isNightTime(assignedStart) || isNightTime(assignedEnd)) {
                        validationErrors.add("Assigned start/end must be within working hours (06:00-22:00)");
                    } else {
                        // Ensure the assigned interval does not pass through night time.
                        // If it does, the raw duration will be greater than the available working time inside that interval.
                        double availableHours = calculateAvailableWorkingHours(assignedStart, assignedEnd);
                        double assignedHours = Duration.between(assignedStart, assignedEnd).toMinutes() / 60.0;

                        if (assignedHours > availableHours + ACCEPTED_ROUNDING_ERROR) {
                            validationErrors.add("Assigned time range must not include night time (22:00-06:00)");
                        }
                    }

                    // Work time estimate must fit into the assigned time range (work-hours aware as well)
                    if (dto.getWorkTimeEstimate() > 0) {
                        double availableHours = calculateAvailableWorkingHours(assignedStart, assignedEnd);

                        if (dto.getWorkTimeEstimate() > availableHours + ACCEPTED_ROUNDING_ERROR) {
                            validationErrors.add(String.format(
                                "Work time estimate (%.2fh) exceeds available working hours (%.2fh) in assigned range (06:00-22:00 daily)",
                                dto.getWorkTimeEstimate(), availableHours
                            ));
                        }
                    }
                }
            }
        }

        if (!validationErrors.isEmpty()) {
            throw new ValidationException("Validation Error", validationErrors);
        }
    }

    private void validateWindowTimeRange(LocalDateTime windowStart,
                                         LocalDateTime windowEnd,
                                         float workTimeEstimate,
                                         List<String> errors) {
        if (windowStart.isAfter(windowEnd)) {
            errors.add("Window end in task must be after window start");
            return;
        }

        // compute the available working hours in the window (06:00-22:00 daily)
        double availableHours = calculateAvailableWorkingHours(windowStart, windowEnd);

        if (workTimeEstimate > availableHours + ACCEPTED_ROUNDING_ERROR) {
            errors.add(String.format(
                "Work time estimate (%.2fh) exceeds available working hours (%.2fh) in window (06:00-22:00 daily)",
                workTimeEstimate, availableHours
            ));
        }
    }

    private double calculateAvailableWorkingHours(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null || !end.isAfter(start)) {
            return 0.0;
        }

        double totalMinutes = 0;
        LocalDate currentDate = start.toLocalDate();
        LocalDate endDate = end.toLocalDate();

        while (!currentDate.isAfter(endDate)) {
            LocalDateTime dayWorkStart = LocalDateTime.of(currentDate, LocalTime.of(WORK_START_HOUR, 0));
            LocalDateTime dayWorkEnd = LocalDateTime.of(currentDate, LocalTime.of(WORK_END_HOUR, 0));

            LocalDateTime effectiveStart = start.isAfter(dayWorkStart) ? start : dayWorkStart;
            LocalDateTime effectiveEnd = end.isBefore(dayWorkEnd) ? end : dayWorkEnd;

            if (effectiveStart.isBefore(effectiveEnd)) {
                totalMinutes += Duration.between(effectiveStart, effectiveEnd).toMinutes();
            }

            currentDate = currentDate.plusDays(1);
        }

        return totalMinutes / 60.0;
    }

    private boolean isNightTime(LocalDateTime dateTime) {
        int hour = dateTime.getHour();
        return hour < WORK_START_HOUR || hour >= WORK_END_HOUR;
    }
}