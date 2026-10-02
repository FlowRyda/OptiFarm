package at.ac.tuwien.ase.groupphase.backend.service.impl.scheduling;

import at.ac.tuwien.ase.groupphase.backend.entity.PlannedTask;
import at.ac.tuwien.ase.groupphase.backend.entity.Schedule;
import at.ac.tuwien.ase.groupphase.backend.entity.Task;
import at.ac.tuwien.ase.groupphase.backend.entity.Tool;
import at.ac.tuwien.ase.groupphase.backend.repository.ActiveFieldRepository;
import at.ac.tuwien.ase.groupphase.backend.service.ForecastService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class GridScheduler {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final List<Task> allTasks;
    private final List<Tool> allTools;
    private final Schedule schedule;
    private final TimeGrid timeGrid;
    private final LocalDateTime now;
    private final ActiveFieldRepository activefieldRepository;
    private final ForecastService forecastService;
    private final PriorityContext priorityContext;
    private final PriorityScorer priorityScorer;
    private final Map<Long, Long> priorityByTaskId;
    // stores which candidates failed to be scheduled due to blockers. Key: Blocker TaskId, Value: Set of "failed candidate tasks"
    private final Map<Long, Set<Task>> blockedCandidates = new HashMap<>();

    private final List<PlannedTask> computedTasks = new ArrayList<>();

    public GridScheduler(List<Task> tasks, List<Tool> tools, Schedule schedule, ActiveFieldRepository activefieldRepository, ForecastService forecastService) {
        this.allTasks = tasks;
        this.allTools = tools;
        this.schedule = schedule;
        this.activefieldRepository = activefieldRepository;
        this.forecastService = forecastService;
        this.priorityContext = new PriorityContext();
        this.priorityScorer = new PriorityScorer();
        this.priorityByTaskId = new HashMap<>();
        this.now = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        this.timeGrid = new TimeGrid(now, 365, allTools);
    }

    private double getTaskValue(Task t) {
        long prio = priorityByTaskId.getOrDefault(t.getId(), 0L);
        return prio * t.getWorkTimeEstimate();
    }

    public void registerExternalBlockers(List<PlannedTask> history) {
        for (PlannedTask pt : history) {
            timeGrid.occupy(pt);
        }
    }

    public List<PlannedTask> compute() {
        // sort out tasks where endDate has already passed
        List<Task> relevantTasks = allTasks.stream()
            .filter(this::isStillRelevant)
            .toList();

        List<Task> flexibleTasks = relevantTasks.stream().filter(t -> !t.isFixed()).toList();

        PriorityContext.PriorityContextData ctx = priorityContext.buildPriorityContext(flexibleTasks, activefieldRepository, forecastService);

        // clear previous blocking relations
        blockedCandidates.clear();

        priorityByTaskId.clear();
        for (Task t : flexibleTasks) {
            priorityByTaskId.put(t.getId(), priorityScorer.score(t, ctx));
        }

        //wichtig: zuerst wird nach Task-Score sortiert, dann nach früheren Windowend und dann nach ID
        List<Task> sortedFlexibleTasks = flexibleTasks.stream()
            .sorted(Comparator.comparingLong((Task t) -> priorityByTaskId.getOrDefault(t.getId(), 0L))
                .reversed()
                .thenComparingDouble(Task::getWorkTimeEstimate) // shortest task first
                .thenComparing(Task::getId)) // final tie-breaker by ID
            .toList();

        List<Task> fixedTasks = relevantTasks.stream().filter(Task::isFixed).toList();
        for (Task task : fixedTasks) {
            scheduleFixedTask(task);
        }

        for (Task task : sortedFlexibleTasks) {
            scheduleFlexibleTask(task);
        }

        return computedTasks;
    }

    private boolean isStillRelevant(Task t) {
        if (t == null) {
            return false;
        }

        LocalDateTime end = t.isFixed() ? t.getAssignedEnd() : t.getWindowEnd();

        return end.isAfter(now);
    }

    private void scheduleFixedTask(Task task) {
        LocalDateTime start = task.getAssignedStart();
        LocalDateTime end = task.getAssignedEnd();

        // only schedule remaining part
        if (start.isBefore(now)) {
            start = now;
        }

        List<Tool> assignedTools = resolveToolsSmart(task, start, end);
        boolean isConflicted = !timeGrid.isSlotAvailable(task.getField(), assignedTools, start, end);

        PlannedTask pt = createPlannedTask(task, start, end, assignedTools, isConflicted);
        timeGrid.occupy(pt);
        computedTasks.add(pt);
    }

    private void scheduleFlexibleTask(Task task) {
        long durationMin = (long) (task.getWorkTimeEstimate() * 60);
        LocalDateTime searchStart = getEffectiveStart(task);

        // Compute Budget
        long flexibleCount = allTasks.stream().filter(t -> !t.isFixed()).count();
        // adjust budget/dynamicOps and depth for runtime vs. scheduler quality tradeoff
        int dynamicOps = 1000 + (int) (flexibleCount * 200);
        int depth = 4;
        OptimizationContext context = new OptimizationContext(dynamicOps, depth);

        boolean solved = recursiveSolve(task, context, 0);

        if (!solved) {
            scheduleWithConflict(task, searchStart, durationMin);
        }
    }

    private boolean recursiveSolve(Task task, OptimizationContext context, int depth) {
        // Guard clauses
        if (depth > context.maxDepth) {
            LOGGER.info("Max depth {} reached for task {}", context.maxDepth, task.getId());
            return false;
        }
        if (!context.consumeOp()) {  //Budget exhausted
            LOGGER.info("Budget exhausted at depth {}", depth);
            return false;
        }
        if (context.visitedTaskIds.contains(task.getId())) {
            LOGGER.info("Task {} already visited in this path", task.getId());
            return false;
        }

        context.visitedTaskIds.add(task.getId());
        long durationMin = (long) (task.getWorkTimeEstimate() * 60);
        LocalDateTime searchStart = getEffectiveStart(task);
        LocalDateTime limit = task.getWindowEnd() != null ? task.getWindowEnd() : computeLimit(searchStart);

        // Try greedy placement first
        Optional<SchedulingResult> freeSlot = findBestSlotGreedy(task, searchStart, limit, durationMin);

        // Greedy Placement succeeded
        if (freeSlot.isPresent()) {
            finalizeBooking(task, freeSlot.get());
            context.visitedTaskIds.remove(task.getId());
            return true;
        }

        // Try to resolve conflicts by deep swapping
        GridCursor cursor = new GridCursor(searchStart, durationMin);

        while (cursor.getEnd().isBefore(limit) || cursor.getEnd().isEqual(limit)) {
            List<PlannedTask> blockers = List.of();
            if (cursor.isValidWorkHours()) {
                blockers = timeGrid.findBlockers(
                    task.getField(),
                    task.getTools(),
                    task.getCategories(),
                    cursor.getStart(),
                    cursor.getEnd(),
                    computedTasks
                );

                int maxBlockers = 4; // Limit to avoid combinatorial explosion
                if (!blockers.isEmpty() && blockers.size() <= maxBlockers) {
                    boolean resolutionPossible = true;

                    // Check if blockers are movable. If not: skip this slot.
                    for (PlannedTask b : blockers) {
                        if (context.visitedTaskIds.contains(b.getTask().getId())) {
                            resolutionPossible = false;
                            break;  //stop checking further blockers
                        }

                        if (b.isFixed()) {
                            if (task.getField() != null && b.getTask().getField() != null && task.getField().getId().equals(b.getTask().getField().getId())) {
                                // Field conflict cannot be resolved
                                resolutionPossible = false;
                                break;
                            }
                            // tool conflict can possibly be resolved
                        }
                    }

                    if (resolutionPossible) {
                        // Take snapshots and release blockers
                        List<TaskSnapshot> snapshots = new ArrayList<>();
                        for (PlannedTask b : blockers) {
                            snapshots.add(TaskSnapshot.capture(b));
                            timeGrid.release(b);
                            computedTasks.remove(b);
                        }

                        // "Ghost booking" of the candidate task
                        List<Tool> toolsForCandidate;

                        //Collect Tools for candidate task
                        if (task.getTools() != null && !task.getTools().isEmpty()) {
                            toolsForCandidate = new ArrayList<>(task.getTools());
                        } else {
                            toolsForCandidate = new ArrayList<>();
                        }
                        if (task.getCategories() != null && !task.getCategories().isEmpty()) {
                            List<Tool> toolsFromCategories = timeGrid.findAvailableTools(task.getCategories(), toolsForCandidate, cursor.getStart(), cursor.getEnd());
                            if (toolsFromCategories == null) {
                                toolsForCandidate = List.of();
                            } else {
                                toolsForCandidate.addAll(toolsFromCategories);
                            }
                        }

                        boolean fieldFree = timeGrid.isFieldAvailable(task.getField(), cursor.getStart(), cursor.getEnd());

                        if (fieldFree && !toolsForCandidate.isEmpty()) {
                            // book the candidate task
                            SchedulingResult tempResult = new SchedulingResult(cursor.getStart(), cursor.getEnd(), toolsForCandidate);
                            finalizeBooking(task, tempResult);

                            // Try to re-schedule the blocker tasks recursively
                            boolean allBlockersMoved = true;

                            List<Task> successfullyMovedTasks = new ArrayList<>();

                            for (PlannedTask blocker : blockers) {
                                boolean blockerMoved = recursiveSolve(blocker.getTask(), context, depth + 1);
                                if (blockerMoved) {
                                    successfullyMovedTasks.add(blocker.getTask());
                                } else {
                                    allBlockersMoved = false;
                                    break;
                                }
                            }

                            if (allBlockersMoved) { // All blockers have new slots, candidate was moved successfully.
                                context.visitedTaskIds.remove(task.getId());
                                return true;
                            } else {                // Some blocker couldn't be moved
                                // Undo candidate move and possible side effects in deeper recursion levels
                                undoRecursiveMove(task, context);

                                // Remove successfully moved blockers
                                for (Task movedBlockerTask : successfullyMovedTasks) {
                                    undoRecursiveMove(movedBlockerTask, context);
                                }
                            }
                        }
                        // Rollback blockers
                        for (int i = 0; i < blockers.size(); i++) {
                            PlannedTask b = blockers.get(i);
                            TaskSnapshot s = snapshots.get(i);

                            s.restoreTo(b);
                            timeGrid.occupy(b);     // Re-book blocker
                            computedTasks.add(b);   // Re-add to computed tasks
                        }
                    }
                }

                if (!blockers.isEmpty() && blockers.size() == 1) {
                    // We need to ensure we haven't modified the grid state in the block above (Rollback handles that)
                    boolean valueSolved = attemptValueBasedReplacement(task, blockers, context);
                    if (valueSolved) {
                        context.visitedTaskIds.remove(task.getId());
                        return true;
                    }
                }
            }
            //after identifying a tool that can't be swapped, but is absolutely required, skip until the first blocker ends, then try again.
            if (!blockers.isEmpty()) {
                LocalDateTime jumpTo = blockers.stream()
                    .map(PlannedTask::getAssignedEnd)
                    .filter(end -> end.isAfter(cursor.getStart()))
                    .min(LocalDateTime::compareTo)
                    .orElse(cursor.getStart().plusMinutes(15));

                cursor.advanceTo(roundUpTo15(jumpTo));
            } else {
                // skip night
                if (!cursor.isValidWorkHours()) {
                    LocalDateTime s = cursor.getStart();
                    LocalDateTime next = s.withHour(6).withMinute(0).withSecond(0).withNano(0);
                    if (!next.isAfter(s)) {
                        next = next.plusDays(1);
                    }
                    cursor.advanceTo(next);
                } else { // base skip 15m
                    cursor.advance(15);
                }
            }
        }
        context.visitedTaskIds.remove(task.getId());
        return false;
    }

    private static LocalDateTime roundUpTo15(LocalDateTime t) {
        int m = t.getMinute();
        int rounded = ((m + 14) / 15) * 15;
        t = t.withSecond(0).withNano(0);
        if (rounded == 60) {
            return t.plusHours(1).withMinute(0);
        }
        return t.withMinute(rounded);
    }

    private boolean attemptValueBasedReplacement(Task candidate, List<PlannedTask> blockers, OptimizationContext context) {
        if (blockers.size() != 1) {
            return false; // Only single-blocker replacement supported
        }

        PlannedTask heavyBlocker = blockers.get(0);

        // cannot replace already visited tasks => would remove a task we inserted as trial earlier in this recursion path, and then returns true and ends all our trys.
        if (context.visitedTaskIds.contains(heavyBlocker.getTask().getId())) {
            return false;
        }

        if (heavyBlocker.getTask().getWorkTimeEstimate() < 2.0) {
            return false;
        }

        double blockerValue = getTaskValue(heavyBlocker.getTask());
        double candidateValue = getTaskValue(candidate);

        double currentDelta = -blockerValue;

        TaskSnapshot blockerSnapshot = TaskSnapshot.capture(heavyBlocker);
        timeGrid.release(heavyBlocker);
        computedTasks.remove(heavyBlocker);

        List<Task> successfullyPlaced = new ArrayList<>();

        LocalDateTime windowStart = heavyBlocker.getAssignedStart();
        LocalDateTime searchStart = getEffectiveStart(candidate).isAfter(windowStart) ? getEffectiveStart(candidate) : windowStart;
        LocalDateTime searchLimit = candidate.getWindowEnd();
        long duration = (long) (candidate.getWorkTimeEstimate() * 60);

        Optional<SchedulingResult> candidateSlot = findBestSlotGreedy(candidate, searchStart, searchLimit, duration);

        if (candidateSlot.isPresent()) {
            finalizeBooking(candidate, candidateSlot.get());
            successfullyPlaced.add(candidate);
            currentDelta += candidateValue;
        } else {
            blockerSnapshot.restoreTo(heavyBlocker);
            timeGrid.occupy(heavyBlocker);
            computedTasks.add(heavyBlocker);
            return false;
        }

        // Try adding "friends" to the filler task

        Set<Task> friends = blockedCandidates.getOrDefault(heavyBlocker.getTask().getId(), new HashSet<>());
        List<Task> potentialFillers = new ArrayList<>();

        for (Task friend : friends) {
            if (!context.visitedTaskIds.contains(friend.getId())) {
                boolean isCurrentlyConflicted = computedTasks.stream()
                    .anyMatch(pt -> pt.getTask().getId().equals(friend.getId()) && pt.isConflicted());

                if (isCurrentlyConflicted) {
                    potentialFillers.add(friend);
                }
            }
        }

        List<PlannedTask> removedConflictedTasks = new ArrayList<>();

        // try high-priority fillers first
        potentialFillers.sort(Comparator.comparingDouble((Task t) -> getTaskValue(t) / t.getWorkTimeEstimate()).reversed());
        for (Task filler : potentialFillers) {
            long fillerDuration = (long) (filler.getWorkTimeEstimate() * 60);
            // Try to place filler
            Optional<SchedulingResult> result = findBestSlotGreedy(filler, filler.getWindowStart(), filler.getWindowEnd(), fillerDuration);

            if (result.isPresent()) {
                // remove old conflicted instance
                Optional<PlannedTask> oldConflicted = computedTasks.stream().filter(pt -> pt.getTask().getId().equals(filler.getId()) && pt.isConflicted()).findFirst();
                if (oldConflicted.isPresent()) {
                    removedConflictedTasks.add(oldConflicted.get());
                    computedTasks.remove(oldConflicted.get());
                }

                finalizeBooking(filler, result.get());
                successfullyPlaced.add(filler);
                currentDelta += getTaskValue(filler);
            }
        }

        // Decision time
        if (currentDelta > 0) {
            // Replacement successful
            scheduleWithConflict(heavyBlocker.getTask(), heavyBlocker.getAssignedStart(), (long) (heavyBlocker.getTask().getWorkTimeEstimate() * 60));

            // Protocol the changes to make them reversible from higher recursion levels
            List<Task> fillersOnly = new ArrayList<>(successfullyPlaced);
            fillersOnly.remove(candidate);
            // moving candidate enabled scheduling the fillers
            context.valueSwapAdditions.put(candidate.getId(), fillersOnly);
            // moving candidate removed this blocker
            context.valueSwapRemovals.put(candidate.getId(), heavyBlocker);

            blockedCandidates.remove(heavyBlocker.getTask().getId());
            return true;
        } else {
            // Rollback
            for (Task placed : successfullyPlaced) {
                releaseLastBookingOf(placed);
            }
            // Re-add any removed conflicted tasks
            computedTasks.addAll(removedConflictedTasks);

            blockerSnapshot.restoreTo(heavyBlocker);
            timeGrid.occupy(heavyBlocker);
            computedTasks.add(heavyBlocker);

            blockedCandidates.computeIfAbsent(heavyBlocker.getTask().getId(), k -> new HashSet<>()).add(candidate);

            return false;
        }
    }

    private void undoRecursiveMove(Task task, OptimizationContext context) {
        // first remove task itself
        releaseLastBookingOf(task);

        // if task caused value-swap, undo that as well
        Long taskId = task.getId();
        if (context.valueSwapAdditions.containsKey(taskId)) {

            // remove fillers first
            List<Task> addedFillers = context.valueSwapAdditions.remove(taskId);
            for (Task filler : addedFillers) {
                releaseLastBookingOf(filler);

                scheduleWithConflict(
                    filler,
                    getEffectiveStart(filler),
                    (long) (filler.getWorkTimeEstimate() * 60)
                );
            }

            // reinstate removed "big blocker task"
            if (context.valueSwapRemovals.containsKey(taskId)) {
                PlannedTask removedBlocker = context.valueSwapRemovals.remove(taskId);

                // Search and remove the "conflicted instance" from computedTasks
                computedTasks.removeIf(pt -> pt.getTask().getId().equals(removedBlocker.getTask().getId()) && pt.isConflicted());

                // reinstate original state of the "big blocker"
                removedBlocker.setConflicted(false);
                timeGrid.occupy(removedBlocker);
                computedTasks.add(removedBlocker);
            }
        }
    }

    private LocalDateTime computeLimit(LocalDateTime start) {
        return start.plusDays(150); // Example: limit to 7 days from start
    }

    private void releaseLastBookingOf(Task t) {
        for (int i = computedTasks.size() - 1; i >= 0; i--) {
            PlannedTask pt = computedTasks.get(i);
            if (pt.getTask().getId().equals(t.getId())) {
                timeGrid.release(pt);
                computedTasks.remove(i);
                return; // find last from now on
            }
        }
    }

    private Optional<SchedulingResult> findBestSlotGreedy(Task task, LocalDateTime start, LocalDateTime endLimit, long durationMin) {
        GridCursor cursor = new GridCursor(start, durationMin);

        boolean useSpecificTools = task.getTools() != null && !task.getTools().isEmpty();

        while (cursor.getEnd().isBefore(endLimit) || cursor.getEnd().isEqual(endLimit)) {
            if (cursor.isValidWorkHours()) {
                boolean fieldFree = timeGrid.isFieldAvailable(task.getField(), cursor.getStart(), cursor.getEnd());

                if (fieldFree) {
                    List<Tool> selectedTools = new ArrayList<>();

                    if (useSpecificTools) {
                        // Check specific tools
                        if (timeGrid.areToolsAvailable(task.getTools(), cursor.getStart(), cursor.getEnd())) {
                            selectedTools = new ArrayList<>(task.getTools());
                        }
                    }
                    // Search Tools by category
                    if (task.getCategories() != null && !task.getCategories().isEmpty()) {
                        List<Tool> toolsFromCategory = timeGrid.findAvailableTools(task.getCategories(), task.getTools(), cursor.getStart(), cursor.getEnd());
                        if (toolsFromCategory != null) {
                            selectedTools.addAll(toolsFromCategory);
                        } else {
                            selectedTools = null; // Not all categories could be fulfilled
                        }
                    }


                    // Tools found
                    if (selectedTools != null && !selectedTools.isEmpty()) {
                        return Optional.of(new SchedulingResult(cursor.getStart(), cursor.getEnd(), selectedTools));
                    }
                }
            }
            cursor.advance(15);
        }
        return Optional.empty();
    }

    private void finalizeBooking(Task task, SchedulingResult result) {
        PlannedTask pt = createPlannedTask(task, result.start, result.end, result.tools, false);
        timeGrid.occupy(pt);
        computedTasks.add(pt);
    }

    private void scheduleWithConflict(Task task, LocalDateTime start, long durationMin) {
        LocalDateTime end = TimeUtil.calculateDurationEnd(start, durationMin);
        PlannedTask pt = createPlannedTask(task, start, end, new ArrayList<>(), true);
        computedTasks.add(pt);
    }

    private PlannedTask createPlannedTask(Task t, LocalDateTime start, LocalDateTime end, List<Tool> tools, boolean conflict) {
        PlannedTask pt = new PlannedTask();
        pt.setTask(t);
        pt.setAssignedStart(start);
        pt.setAssignedEnd(end);
        List<Tool> toolList = (tools != null) ? new ArrayList<>(tools) : new ArrayList<>(); // null-safety
        pt.setFixedEquipment(new ArrayList<>(toolList));
        pt.setConflicted(conflict);
        pt.setFixed(t.isFixed());
        pt.setSchedule(schedule);
        pt.setOwner(schedule.getOwner());
        return pt;
    }

    private List<Tool> resolveToolsSmart(Task task, LocalDateTime start, LocalDateTime end) {
        List<Tool> assignedTools = new ArrayList<>();

        if (task.getTools() != null && !task.getTools().isEmpty()) {
            for (Tool t : task.getTools()) {
                if (t != null && t.isUsable()) {
                    assignedTools.add(t);
                }
            }
        }

        if (task.getCategories() != null && !task.getCategories().isEmpty()) {
            List<Tool> categoryTools = timeGrid.findAvailableTools(task.getCategories(), task.getTools(), start, end);
            assignedTools.addAll(categoryTools);
        }
        return assignedTools;
    }

    private LocalDateTime getEffectiveStart(Task t) {
        return (t.getWindowStart() != null && t.getWindowStart().isAfter(now)) ? t.getWindowStart() : now;
    }

    private record SchedulingResult(LocalDateTime start, LocalDateTime end, List<Tool> tools) {
    }

    private static class OptimizationContext {
        int opsRemaining;
        final int maxDepth;
        final Set<Long> visitedTaskIds;

        // Key: ID of the task that triggered the swap
        final Map<Long, List<Task>> valueSwapAdditions = new HashMap<>();
        final Map<Long, PlannedTask> valueSwapRemovals = new HashMap<>();

        public OptimizationContext(int maxOps, int maxDepth) {
            this.opsRemaining = maxOps;
            this.maxDepth = maxDepth;
            this.visitedTaskIds = new HashSet<>();
        }

        public boolean consumeOp() {
            if (opsRemaining <= 0) {
                return false;
            }
            opsRemaining--;
            return true;
        }
    }

    private record TaskSnapshot(
        LocalDateTime start,
        LocalDateTime end,
        List<Tool> fixedEquipment,
        boolean isConflicted
    ) {
        public static TaskSnapshot capture(PlannedTask pt) {
            List<Tool> toolsCopy = (pt.getFixedEquipment() != null) //Deep copy of the list.
                ? new ArrayList<>(pt.getFixedEquipment())
                : new ArrayList<>();
            return new TaskSnapshot(pt.getAssignedStart(), pt.getAssignedEnd(), toolsCopy, pt.isConflicted());
        }

        public void restoreTo(PlannedTask pt) {
            pt.setAssignedStart(this.start);
            pt.setAssignedEnd(this.end);
            pt.setFixedEquipment(new ArrayList<>(this.fixedEquipment)); // Set new list to avoid side-effects
            pt.setConflicted(this.isConflicted);
        }
    }
}