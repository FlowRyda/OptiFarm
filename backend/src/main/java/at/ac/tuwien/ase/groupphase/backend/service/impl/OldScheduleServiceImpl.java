package at.ac.tuwien.ase.groupphase.backend.service.impl;

import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.schedule.PlannedTaskDto;
import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.schedule.ScheduleDto;
import at.ac.tuwien.ase.groupphase.backend.endpoint.mapper.ScheduleMapper;
import at.ac.tuwien.ase.groupphase.backend.entity.Farmer;
import at.ac.tuwien.ase.groupphase.backend.entity.PlannedTask;
import at.ac.tuwien.ase.groupphase.backend.entity.Schedule;
import at.ac.tuwien.ase.groupphase.backend.entity.Task;
import at.ac.tuwien.ase.groupphase.backend.entity.Tool;
import at.ac.tuwien.ase.groupphase.backend.entity.ToolCategory;
import at.ac.tuwien.ase.groupphase.backend.exception.AccessDeniedException;
import at.ac.tuwien.ase.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.ase.groupphase.backend.repository.FarmerRepository;
import at.ac.tuwien.ase.groupphase.backend.repository.PlannedTaskRepository;
import at.ac.tuwien.ase.groupphase.backend.repository.ScheduleRepository;
import at.ac.tuwien.ase.groupphase.backend.repository.TaskRepository;
import at.ac.tuwien.ase.groupphase.backend.repository.ToolRepository;
import at.ac.tuwien.ase.groupphase.backend.security.JwtUtil;
import at.ac.tuwien.ase.groupphase.backend.service.ScheduleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.invoke.MethodHandles;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class OldScheduleServiceImpl implements ScheduleService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static final int WORK_START_HOUR = 6;
    private static final int WORK_END_HOUR = 22;

    private final ScheduleMapper scheduleMapper;
    private final ScheduleRepository scheduleRepository;
    private final TaskRepository taskRepository;
    private final FarmerRepository farmerRepository;
    private final PlannedTaskRepository plannedTaskRepository;
    private final ToolRepository toolRepository;


    @Autowired
    public OldScheduleServiceImpl(ScheduleRepository scheduleRepository, TaskRepository taskRepository,
                                  PlannedTaskRepository plannedTaskRepository, FarmerRepository farmerRepository,
                                  ToolRepository toolRepository, ScheduleMapper scheduleMapper) {
        this.scheduleRepository = scheduleRepository;
        this.taskRepository = taskRepository;
        this.plannedTaskRepository = plannedTaskRepository;
        this.farmerRepository = farmerRepository;
        this.toolRepository = toolRepository;
        this.scheduleMapper = scheduleMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public ScheduleDto getScheduleById(Long id) {
        Optional<Schedule> schedule = scheduleRepository.findByIdAndOwner(id, getFarmer());
        return schedule.map(scheduleMapper::toDto).orElseThrow(() -> {
            LOGGER.info("Schedule with id {} not found", id);
            return new NotFoundException(String.format("Could not find a schedule with the id %s", id));
        });
    }

    @Override
    @Transactional(readOnly = true)
    public List<ScheduleDto> getAllSchedules() {
        List<Schedule> schedules = scheduleRepository.findAllByOwner(getFarmer());
        return scheduleMapper.toDtoList(schedules);
    }

    @Override
    @Transactional
    public void deleteSchedule(Long id) throws NotFoundException, AccessDeniedException {
        LOGGER.info("Delete schedule with id {}", id);
        var farmer = getFarmer();

        var schedule = scheduleRepository.findById(id).orElseThrow(() ->
            new NotFoundException("Schedule to delete was not found")
        );

        if (!schedule.getOwner().getId().equals(farmer.getId())) {
            throw new AccessDeniedException("You do not have permission to delete this schedule");
        }

        //clears planned_task_tool join table entries
        for (PlannedTask pt : schedule.getPlannedTasks()) {
            pt.getFixedEquipment().clear();
        }

        scheduleRepository.delete(schedule);
    }

    @Override
    @Transactional
    public ScheduleDto computeNewSchedule() {
        Schedule schedule = new Schedule();
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);

        //setting basic schedule proporties
        String name = "Schedule " + now;
        schedule.setName(name);
        schedule.setOwner(getFarmer());
        schedule.setOwnerId(getFarmer().getId());

        // save first to generate id
        schedule = scheduleRepository.save(schedule);

        //get all the users tasks
        List<Task> tasks = taskRepository.findAllByOwner(getFarmer());

        List<Task> fixedTasks = tasks.stream()
            .filter(Task::isFixed)
            .sorted(Comparator.comparing(Task::getAssignedStart, Comparator.nullsLast(Comparator.naturalOrder())))
            .toList();

        List<Task> flexibleTasks = tasks.stream()
            .filter(t -> !t.isFixed())
            .sorted(Comparator.comparing(Task::getWindowEnd))
            .toList();

        List<PlannedTask> planned = new ArrayList<>();

        // schedule fixed first (blockers empty at first)
        planned.addAll(scheduleNewTasks(fixedTasks, planned, schedule));

        // then schedule non-fixed around them
        planned.addAll(scheduleNewTasks(flexibleTasks, planned, schedule));

        /*tasks.sort(Comparator.comparing(Task::getWindowEnd));

        //Plan the tasks in scheduler (naive implementation)
        List<PlannedTask> plannedTasks = scheduleNewTasks(tasks, new ArrayList<>(), schedule);

        // persist planned tasks
        plannedTasks = plannedTaskRepository.saveAll(plannedTasks);
        schedule.setPlannedTasks(plannedTasks);
        */

        planned = plannedTaskRepository.saveAll(planned);
        schedule.setPlannedTasks(planned);

        // after planned is fully built (fixed + flexible) but before saving
        for (int i = 0; i < planned.size(); i++) {
            for (int j = i + 1; j < planned.size(); j++) {
                PlannedTask a = planned.get(i);
                PlannedTask b = planned.get(j);

                if (hasConflict(a, b)) {
                    a.setConflicted(true);
                    b.setConflicted(true);
                }
            }
        }

        //save schedule
        schedule = scheduleRepository.save(schedule);
        LOGGER.info("Computed new schedule with id {}", schedule.getId());

        return scheduleMapper.toDto(schedule);
    }


    @Override
    @Transactional
    public ScheduleDto recomputeSchedule(ScheduleDto currentSchedule) {
        Schedule scheduleEntity = scheduleRepository.findByIdAndOwner(currentSchedule.getId(), getFarmer())
            .orElseThrow(() -> new NotFoundException("No schedule found with id " + currentSchedule.getId()));

        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);

        List<PlannedTask> all = new ArrayList<>(scheduleEntity.getPlannedTasks());

        // 1) Split past vs future (keep past unchanged)
        List<PlannedTask> past = all.stream()
            .filter(pt -> pt.getAssignedEnd() != null && !pt.getAssignedEnd().isAfter(now))
            .toList();

        List<PlannedTask> future = all.stream()
            .filter(pt -> pt.getAssignedEnd() == null || pt.getAssignedEnd().isAfter(now))
            .toList();

        // Reset conflicts for future tasks (we will recompute them)
        for (PlannedTask pt : future) {
            pt.setConflicted(false);
        }

        // 2) Re-place future tasks: fixed first, then flexible around them
        List<PlannedTask> fixedFuture = future.stream()
            .filter(PlannedTask::isFixed)
            .sorted(Comparator.comparing(PlannedTask::getAssignedStart, Comparator.nullsLast(Comparator.naturalOrder())))
            .toList();

        List<PlannedTask> flexibleFuture = future.stream()
            .filter(pt -> !pt.isFixed())
            .sorted(Comparator.comparing(pt -> pt.getTask().getWindowEnd(), Comparator.nullsLast(Comparator.naturalOrder())))
            .toList();

        // Blockers are what already occupies resources (field/tools)
        List<PlannedTask> blockers = new ArrayList<>(past);
        blockers.addAll(fixedFuture);

        // Re-place ALL flexible tasks in the future around fixed tasks
        for (PlannedTask pt : flexibleFuture) {
            Task task = pt.getTask();
            long durationMinutes = (long) (task.getWorkTimeEstimate() * 60);

            LocalDateTime searchStart = getEarliestPossibleStart(task);
            TimeSlot slot = findFirstFreeSlot(
                task,
                searchStart,
                durationMinutes,
                blockers,
                pt.getFixedEquipment() // tools assigned to this planned task
            );

            // If we cannot fit inside window, fallback to "conflicted forced slot" like before
            if (isTimeOutsideWindow(slot.end(), task.getWindowEnd())) {
                LocalDateTime forcedEnd = now;
                LocalDateTime forcedStart = now.minusMinutes(durationMinutes);
                pt.setAssignedStart(forcedStart);
                pt.setAssignedEnd(forcedEnd);
                pt.setConflicted(true);
            } else {
                pt.setAssignedStart(slot.start());
                pt.setAssignedEnd(slot.end());
                pt.setConflicted(false);
            }

            blockers.add(pt);
        }

        // 3) Recompute conflict flags for future (mainly fixed-fixed or forced fallbacks)
        //    - window violations
        for (PlannedTask pt : future) {
            if (pt.getAssignedEnd() != null && isTimeOutsideWindow(pt.getAssignedEnd(), pt.getTask().getWindowEnd())) {
                pt.setConflicted(true);
            }
        }

        //    - overlaps (field/tools) that still exist
        //      (should be rare except fixed-fixed or forced)
        for (int i = 0; i < future.size(); i++) {
            for (int j = i + 1; j < future.size(); j++) {
                PlannedTask a = future.get(i);
                PlannedTask b = future.get(j);

                if (hasConflict(a, b)) {
                    if (a.isFixed() && b.isFixed()) {
                        a.setConflicted(true);
                        b.setConflicted(true);
                    } else if (a.isFixed()) {
                        b.setConflicted(true);
                    } else if (b.isFixed()) {
                        a.setConflicted(true);
                    } else {
                        // both flexible -> if this happens it's basically only due to forced fallbacks
                        a.setConflicted(true);
                        b.setConflicted(true);
                    }
                }
            }
        }

        List<PlannedTask> merged = new ArrayList<>();
        merged.addAll(past);
        merged.addAll(future);

        plannedTaskRepository.saveAll(merged);

        scheduleEntity.getPlannedTasks().clear();
        scheduleEntity.getPlannedTasks().addAll(merged);

        for (PlannedTask pt : scheduleEntity.getPlannedTasks()) {
            pt.setSchedule(scheduleEntity);
        }

        // 5) Schedule new/unplanned tasks (unchanged from your old logic)
        List<Task> allOwnerTasks = taskRepository.findAllByOwner(getFarmer());

        Set<Long> scheduledTaskIds = merged.stream()
            .map(pt -> pt.getTask().getId())
            .collect(Collectors.toSet());

        List<Task> tasksToSchedule = allOwnerTasks.stream()
            .filter(t -> !scheduledTaskIds.contains(t.getId()))
            .sorted(Comparator.comparing(Task::getWindowEnd))
            .toList();

        List<PlannedTask> newPlannedTasks = scheduleNewTasks(tasksToSchedule, merged, scheduleEntity);

        merged.addAll(newPlannedTasks);
        plannedTaskRepository.saveAll(merged);

        scheduleEntity.getPlannedTasks().clear();
        scheduleEntity.getPlannedTasks().addAll(merged);

        for (PlannedTask pt : scheduleEntity.getPlannedTasks()) {
            pt.setSchedule(scheduleEntity);
        }

        scheduleEntity = scheduleRepository.save(scheduleEntity);

        return scheduleMapper.toDto(scheduleEntity);
    }

    private List<PlannedTask> scheduleNewTasks(List<Task> tasks, List<PlannedTask> blockers, Schedule schedule) {
        List<PlannedTask> result = new ArrayList<>();
        List<PlannedTask> currentBlockers = new ArrayList<>(blockers);
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);

        for (Task t : tasks) {
            List<Tool> tools = resolveToolsForTask(t, getFarmer());
            long durationMinutes = (long) (t.getWorkTimeEstimate() * 60);

            boolean fixed = t.isFixed();
            boolean isConflicted = false;
            TimeSlot slot;

            if (fixed) {
                LocalDateTime start = t.getAssignedStart();
                LocalDateTime end = t.getAssignedEnd();

                slot = new TimeSlot(start, end);

                // Mark conflict if outside window
                if (isTimeOutsideWindow(slot.end, t.getWindowEnd())) {
                    isConflicted = true;
                }

                // Also mark conflict if overlaps blockers for same field/tools
                if (overlapsBlockers(t, slot.start, slot.end, currentBlockers, tools)) {
                    isConflicted = true;
                }

                PlannedTask pt = new PlannedTask(slot.start, slot.end, t, tools, true, isConflicted, schedule);
                pt.setOwner(getFarmer());
                pt.setOwnerId(getFarmer().getId());

                result.add(pt);
                currentBlockers.add(pt); // fixed tasks block others
                continue;
            }

            LocalDateTime searchStart = getEarliestPossibleStart(t);
            slot = findFirstFreeSlot(t, searchStart, durationMinutes, currentBlockers, tools);

            // If outside allowed window -> conflict fallback
            if (isTimeOutsideWindow(slot.end, t.getWindowEnd())) {
                LocalDateTime forcedEnd = now;
                LocalDateTime forcedStart = now.minusMinutes(durationMinutes);
                slot = new TimeSlot(forcedStart, forcedEnd);
                isConflicted = true;
            }

            PlannedTask pt = new PlannedTask(slot.start, slot.end, t, tools, false, isConflicted, schedule);
            pt.setOwner(getFarmer());
            pt.setOwnerId(getFarmer().getId());

            result.add(pt);
            currentBlockers.add(pt);
        }
        return result;
    }

    private boolean attemptToMoveTask(PlannedTask pt, List<PlannedTask> allTasks) {
        if (pt.isFixed()) {
            return false;
        }

        List<PlannedTask> blockers = new ArrayList<>(allTasks);
        blockers.remove(pt); // Exclude self from blockers

        long duration = (long) pt.getTask().getWorkTimeEstimate() * 60;

        // Search for a new slot starting from window start
        LocalDateTime searchStart = getEarliestPossibleStart(pt.getTask());

        TimeSlot newSlot = findFirstFreeSlot(pt.getTask(), searchStart, duration, blockers, pt.getFixedEquipment());

        // Validate if new slot is within window
        if (!isTimeOutsideWindow(newSlot.end, pt.getTask().getWindowEnd())) {
            // Success: update the task
            pt.setAssignedStart(newSlot.start);
            pt.setAssignedEnd(newSlot.end);
            pt.setConflicted(false); // Clear conflict
            return true;
        }
        return false;
    }

    /**
     * Helper method to ensure we never plan in the past.
     *
     * @return the earliest possible start time (given from window start and now)
     */
    private LocalDateTime getEarliestPossibleStart(Task t) {
        LocalDateTime start = t.getWindowStart();
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        if (start.isBefore(now)) {
            start = LocalDateTime.now();
        }
        return adjustStartToWorkHours(start);
    }

    private TimeSlot findFirstFreeSlot(Task task, LocalDateTime startSearch, long durationMinutes,
                                       List<PlannedTask> alreadyPlanned, List<Tool> neededTools) {
        LocalDateTime currentStart = startSearch;
        LocalDateTime currentEnd = calculateEndBasedOnWorkHours(currentStart, durationMinutes);

        boolean collisionFound = true;
        LocalDateTime safetyLimit = startSearch.plusYears(1);
        int trialCount = 0;

        // Try to find a slot. Stop if we go too far into the future (safety limit)
        while (collisionFound && currentStart.isBefore(safetyLimit) && trialCount < 500) {
            collisionFound = false;

            for (PlannedTask pt : alreadyPlanned) {
                // Ignore self-check implicitly handled by caller list management usually,
                // but checking IDs ensures safety
                if (pt.getTask().getId().equals(task.getId())) {
                    continue;
                }

                if (intervalsOverlap(currentStart, currentEnd, pt.getAssignedStart(), pt.getAssignedEnd())) {
                    boolean sameField = pt.getTask().getField() != null && pt.getTask().getField().equals(task.getField());
                    boolean shareTools = toolsOverlap(neededTools, pt.getFixedEquipment());

                    if (sameField || shareTools) {
                        collisionFound = true;
                        // Jump to end of conflicting task
                        currentStart = adjustStartToWorkHours(pt.getAssignedEnd());
                        currentEnd = calculateEndBasedOnWorkHours(currentStart, durationMinutes);
                        break; // Restart check with new time
                    }
                }
            }
            trialCount++;
        }
        return new TimeSlot(currentStart, currentEnd);
    }

    /**
     * Assigns the first tool in each required category to the task.
     *
     * @param task  the task
     * @param owner the farmer owning the task
     * @return list of tools assigned to the task
     */
    private List<Tool> resolveToolsForTask(Task task, Farmer owner) {
        if (task.getTools() != null && !task.getTools().isEmpty()) {
            return new ArrayList<>(task.getTools());
        }
        List<Tool> requiredTools = new ArrayList<>();

        List<ToolCategory> categories = task.getCategories();
        if (categories != null) {
            for (ToolCategory c : categories) {
                List<Tool> toolsInCategory = toolRepository.findByOwnerAndCategory(owner, c);
                toolsInCategory = toolsInCategory.stream().filter(Tool::isUsable).toList();
                if (!toolsInCategory.isEmpty()) {
                    // NAIVE: we return any tool (the first one) in the category
                    requiredTools.add(toolsInCategory.getFirst());
                }
            }
        }
        return requiredTools;
    }

    private boolean toolsOverlap(List<Tool> list1, List<Tool> list2) {
        if (list1 == null || list2 == null) {
            return false;
        }
        // Check if lists share any element (based on ID)
        return list1.stream().anyMatch(t1 -> list2.stream().anyMatch(t2 -> t2.getId().equals(t1.getId())));
    }

    private LocalDateTime calculateEndBasedOnWorkHours(LocalDateTime start, long durationMinutes) {
        LocalDateTime pointer = start;
        long minutesLeft = durationMinutes;

        // Adjust start time if outside work hours
        if (pointer.getHour() < WORK_START_HOUR) {
            pointer = pointer.withHour(WORK_START_HOUR).withMinute(0);
        } else if (pointer.getHour() >= WORK_END_HOUR) {
            pointer = pointer.plusDays(1).withHour(WORK_START_HOUR).withMinute(0);
        }

        while (minutesLeft > 0) {
            // End of first day
            LocalDateTime endOfShift = pointer.toLocalDate().atTime(WORK_END_HOUR, 0);

            // minutes until end of shift
            long minutesUntilShiftEnd = ChronoUnit.MINUTES.between(pointer, endOfShift);

            if (minutesLeft <= minutesUntilShiftEnd) {
                // fits in the remaining time of the day
                return pointer.plusMinutes(minutesLeft);
            } else {
                // second day required
                minutesLeft -= minutesUntilShiftEnd;
                // start of next work day
                pointer = pointer.plusDays(1)
                    .withHour(WORK_START_HOUR)
                    .withMinute(0)
                    .withSecond(0)
                    .withNano(0);
            }
        }
        return pointer;
    }

    private LocalDateTime adjustStartToWorkHours(LocalDateTime rawStart) {
        if (rawStart.getHour() >= WORK_END_HOUR) {
            return rawStart.plusDays(1).withHour(WORK_START_HOUR).withMinute(0).withSecond(0).withNano(0);
        }
        if (rawStart.getHour() < WORK_START_HOUR) {
            return rawStart.withHour(WORK_START_HOUR).withMinute(0).withSecond(0).withNano(0);
        }
        return rawStart;
    }

    private LocalDateTime getEffectiveWindowEnd(LocalDateTime windowEnd) {
        if (windowEnd == null) {
            return LocalDateTime.MAX;
        }

        // Wenn genau 00:00 Uhr, gehen wir davon aus, dass der Tag inklusive ist -> Ende des Tages
        if (windowEnd.getHour() == 0 && windowEnd.getMinute() == 0) {
            return windowEnd.plusDays(1).minusSeconds(1);
        }
        return windowEnd;
    }

    private boolean isTimeOutsideWindow(LocalDateTime plannedEnd, LocalDateTime windowEnd) {
        LocalDateTime effectiveEnd = getEffectiveWindowEnd(windowEnd);

        return plannedEnd.isAfter(effectiveEnd);
    }

    private record TimeSlot(LocalDateTime start, LocalDateTime end) {
    }

    private boolean hasConflict(PlannedTask pt1, PlannedTask pt2) {
        boolean timeOverlap = intervalsOverlap(pt1.getAssignedStart(), pt1.getAssignedEnd(),
            pt2.getAssignedStart(), pt2.getAssignedEnd());
        if (!timeOverlap) {
            return false;
        }

        boolean sameField = pt1.getTask().getField() != null && pt1.getTask().getField().equals(pt2.getTask().getField());
        boolean shareTools = toolsOverlap(pt1.getFixedEquipment(), pt2.getFixedEquipment());

        return sameField || shareTools;
    }


    private boolean intervalsOverlap(LocalDateTime start1, LocalDateTime end1, LocalDateTime start2, LocalDateTime end2) {
        return start1.isBefore(end2) && start2.isBefore(end1);
    }

    private Farmer getFarmer() {
        var activeUser = JwtUtil.getActiveJwtUser();

        return farmerRepository.findById(activeUser.id()).orElseThrow(() -> {
            LOGGER.info("Farmer with id {} not found", activeUser.id());
            return new NotFoundException(String.format("Could not find a farmer with the id %s", activeUser.id()));
        });
    }

    @Override
    @Transactional
    public ScheduleDto updateSchedule(Long id, ScheduleDto scheduleDto) {
        LOGGER.info("Updating schedule with id {}", id);

        Schedule schedule = scheduleRepository.findByIdAndOwner(id, getFarmer())
            .orElseThrow(() -> new NotFoundException("No schedule found with id " + id));

        if (scheduleDto.getName() != null) {
            schedule.setName(scheduleDto.getName());
        }

        if (scheduleDto.getPlannedTasks() != null) {
            for (PlannedTaskDto plannedTaskDto : scheduleDto.getPlannedTasks()) {
                PlannedTask plannedTask = schedule.getPlannedTasks().stream()
                    .filter(pt -> pt.getId().equals(plannedTaskDto.getId()))
                    .findFirst()
                    .orElse(null);

                if (plannedTask != null) {
                    if (plannedTaskDto.getAssignedStart() != null) {
                        plannedTask.setAssignedStart(plannedTaskDto.getAssignedStart());
                    }
                    if (plannedTaskDto.getAssignedEnd() != null) {
                        plannedTask.setAssignedEnd(plannedTaskDto.getAssignedEnd());
                    }

                    plannedTask.setFixed(plannedTaskDto.isFixed());

                    Task underlyingTask = plannedTask.getTask();
                    if (underlyingTask != null) {
                        underlyingTask.setFixed(plannedTaskDto.isFixed());

                        if (plannedTaskDto.getAssignedStart() != null && plannedTaskDto.getAssignedEnd() != null) {
                            underlyingTask.setAssignedStart(plannedTaskDto.getAssignedStart());
                            underlyingTask.setAssignedEnd(plannedTaskDto.getAssignedEnd());
                        }

                        taskRepository.save(underlyingTask);
                    }
                }
            }

            plannedTaskRepository.saveAll(schedule.getPlannedTasks());
        }

        schedule = scheduleRepository.save(schedule);
        LOGGER.info("Successfully updated schedule with id {}", id);

        return scheduleMapper.toDto(schedule);
    }

    @Override
    @Transactional
    public ScheduleDto saveSchedule(ScheduleDto scheduleDto) {
        LOGGER.info("Saving schedule with name: {}", scheduleDto.getName());

        Schedule existingSchedule = scheduleRepository.findByIdAndOwner(scheduleDto.getId(), getFarmer())
            .orElseThrow(() -> new NotFoundException("No schedule found with id " + scheduleDto.getId()));

        if (scheduleDto.getName() != null && !scheduleDto.getName().trim().isEmpty()) {
            existingSchedule.setName(scheduleDto.getName().trim());
        }

        Schedule savedSchedule = scheduleRepository.save(existingSchedule);
        LOGGER.info("Successfully saved schedule with id {} and name {}", savedSchedule.getId(), savedSchedule.getName());

        return scheduleMapper.toDto(savedSchedule);
    }

    private boolean overlapsBlockers(Task t, LocalDateTime start, LocalDateTime end,
                                     List<PlannedTask> blockers, List<Tool> neededTools) {
        for (PlannedTask pt : blockers) {
            if (!intervalsOverlap(start, end, pt.getAssignedStart(), pt.getAssignedEnd())) {
                continue;
            }

            boolean sameField =
                pt.getTask().getField() != null
                    && pt.getTask().getField().equals(t.getField());

            boolean shareTools = toolsOverlap(neededTools, pt.getFixedEquipment());

            if (sameField || shareTools) {
                return true;
            }
        }
        return false;
    }
}
