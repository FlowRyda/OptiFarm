package at.ac.tuwien.ase.groupphase.backend.basetest;

import at.ac.tuwien.ase.groupphase.backend.entity.*;
import at.ac.tuwien.ase.groupphase.backend.repository.ActiveFieldRepository;
import at.ac.tuwien.ase.groupphase.backend.service.ForecastService;
import at.ac.tuwien.ase.groupphase.backend.service.impl.scheduling.GridScheduler;
import at.ac.tuwien.ase.groupphase.backend.service.impl.scheduling.PriorityScorer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class GridSchedulerTest {

    @Mock
    private ActiveFieldRepository activeFieldRepository;
    @Mock
    private ForecastService forecastService;

    private Schedule schedule;
    private Farmer owner;
    private Field field1;
    private Field field2;
    private LocalDateTime baseTime;

    @BeforeEach
    void setUp() {
        owner = new Farmer();
        owner.setId(1L);
        schedule = new Schedule();
        schedule.setOwner(owner);
        schedule.setPlannedTasks(new ArrayList<>());

        field1 = new Field();
        field1.setId(100L);
        field1.setName("Test Field");

        field2 = new Field();
        field2.setId(200L);
        field2.setName("Second Field");

        // Wir starten "morgen" um 08:00 Uhr
        baseTime = LocalDateTime.now().plusDays(1).withHour(8).withMinute(0).withSecond(0).withNano(0);

        // Mocks so konfigurieren, dass sie nicht crashen (Priorität ist uns hier egal)
        lenient().when(activeFieldRepository.findAllByOwner(any())).thenReturn(Collections.emptyList());
    }

    /**
     * SZENARIO 1: Der klassische "Time Swap"
     * Task 1 (flexibel) liegt idealerweise um 08:00.
     * Task 2 (flexibel, aber höhere Prio durch ID/Sortierung simuliert) will auch um 08:00.
     * * Erwartung: Task 1 wird zur Seite geschoben (z.B. auf 10:00), damit Task 2 um 08:00 stattfinden kann.
     * (Oder andersherum, je nach Sortierung - wichtig ist: KEIN Konflikt).
     */
    @Test
    void testRecursiveTimeSwap() {
        // --- TOOLS ---
        ToolCategory tractorCat = new ToolCategory();
        tractorCat.setId(1L);
        tractorCat.setName("Tractors");

        Tool tractor1 = createTool(1L, "Tractor 1", tractorCat);
        List<Tool> allTools = List.of(tractor1); // Nur 1 Traktor verfügbar!

        // --- TASKS ---
        // Task A: Dauert 2h, will Traktor
        Task taskA = createTask(1L, "Task A (Low Prio)", 2, field1);
        taskA.setCategories(List.of(tractorCat));
        taskA.setWindowStart(baseTime); // Will ab 08:00

        // Task B: Dauert 2h, will Traktor
        Task taskB = createTask(2L, "Task B (High Prio)", 2, field1);
        taskB.setCategories(List.of(tractorCat));
        taskB.setWindowStart(baseTime); // Will ab 08:00

        List<Task> tasks = new ArrayList<>();
        tasks.add(taskA);
        tasks.add(taskB);

        // --- EXECUTE ---
        GridScheduler scheduler = new GridScheduler(tasks, allTools, schedule, activeFieldRepository, forecastService);
        List<PlannedTask> result = scheduler.compute();

        // --- VERIFY ---
        printSchedule(result);

        assertEquals(2, result.size());

        PlannedTask ptA = findPlannedTask(result, 1L);
        PlannedTask ptB = findPlannedTask(result, 2L);

        assertFalse(ptA.isConflicted(), "Task A sollte konfliktfrei sein");
        assertFalse(ptB.isConflicted(), "Task B sollte konfliktfrei sein");

        // Da nur 1 Traktor da ist, dürfen sie sich nicht überschneiden
        boolean overlap = isOverlapping(ptA, ptB);
        assertFalse(overlap, "Tasks dürfen sich zeitlich nicht überschneiden, da nur 1 Tool da ist!");
    }

    /**
     * SZENARIO 2: Der "Resource Swap" (Ghost Booking Test)
     * Task A belegt "Traktor 1".
     * Task B BRAUCHT zwingend "Traktor 1".
     * "Traktor 2" ist aber frei.
     * * Erwartung: Der Scheduler merkt, dass er Task A einfach auf "Traktor 2" schieben kann,
     * ohne die Zeit zu ändern, damit B auf "Traktor 1" kann.
     */
    @Test
    void testResourceSwap() {
        // --- TOOLS ---
        float duration = 2.0f;
        ToolCategory tractorCat = new ToolCategory();
        tractorCat.setName("Tractors");

        Tool tractor1 = createTool(1L, "Tractor 1", tractorCat);
        Tool tractor2 = createTool(2L, "Tractor 2", tractorCat);
        List<Tool> allTools = List.of(tractor1, tractor2);

        // --- TASKS ---
        // Task A: Ist flexibel bei Tools (Kategorie), wird aber vermutlich zuerst auf T1 gebucht (Greedy)
        Task taskA = createTask(1L, "Task A (Flexible Tool)", duration, field1);
        taskA.setCategories(List.of(tractorCat));
        taskA.setWindowStart(baseTime);
        taskA.setWindowEnd(baseTime.plusHours((long)duration));

        // Task B: Braucht ZWINGEND Tractor 1
        Task taskB = createTask(2L, "Task B (Needs T1)", duration, field2);
        taskB.setTools(List.of(tractor1)); // Specific Tool Requirement!
        taskB.setWindowStart(baseTime);
        taskB.setWindowEnd(baseTime.plusHours((long)duration));

        // Wir sorgen dafür, dass Task A zuerst gescheduled wird (z.B. über ID oder Liste)
        List<Task> tasks = new ArrayList<>();
        tasks.add(taskA);
        tasks.add(taskB);

        // --- EXECUTE ---
        GridScheduler scheduler = new GridScheduler(tasks, allTools, schedule, activeFieldRepository, forecastService);
        List<PlannedTask> result = scheduler.compute();

        // --- VERIFY ---
        printSchedule(result);

        PlannedTask ptA = findPlannedTask(result, 1L);
        PlannedTask ptB = findPlannedTask(result, 2L);

        assertFalse(ptA.isConflicted());
        assertFalse(ptB.isConflicted());

        // Beide sollten zur gleichen Zeit stattfinden (08:00 - 10:00), da wir 2 Tools haben
        assertEquals(ptA.getAssignedStart(), ptB.getAssignedStart(), "Tasks sollten parallel laufen");

        // Check Tools
        assertTrue(ptB.getFixedEquipment().contains(tractor1), "Task B muss Traktor 1 haben");
        assertTrue(ptA.getFixedEquipment().contains(tractor2), "Task A sollte auf Traktor 2 ausgewichen sein");
    }

    /**
     * SZENARIO 3: Konflikt unvermeidbar
     * 1 Tool, 2 Tasks, beide Fixed auf die gleiche Zeit.
     * Oder: 1 Tool, Zeitfenster extrem klein.
     */
    @Test
    void testUnsolvableConflict() {
        ToolCategory cat = new ToolCategory();
        cat.setName("ToolCategory 1");
        cat.setId(1L);
        cat.setOwner(owner);
        cat.setOwnerId(owner.getId());
        Tool tool1 = createTool(1L, "Solo Tool", cat);
        List<Tool> allTools = List.of(tool1);

        // Task A: 08:00 - 12:00 (Fixed)
        Task taskA = createTask(1L, "Fixed Task", 12, field1);
        taskA.setFixed(true);
        taskA.setAssignedStart(baseTime);
        taskA.setAssignedEnd(baseTime.plusHours(12));
        taskA.setCategories(List.of(cat));

        // Task B: Will auch 08:00 - 10:00, Fenster endet aber um 10:00
        // -> Kann nicht verschoben werden, weil WindowEnd erreicht wird.
        Task taskB = createTask(2L, "Flexible but constrained", 2, field1);
        taskB.setWindowStart(baseTime);
        taskB.setWindowEnd(baseTime.plusHours(2)); // Fenster ist exakt so groß wie Dauer -> keine Verschiebung möglich
        taskB.setCategories(List.of(cat));

        List<Task> tasks = List.of(taskA, taskB);

        GridScheduler scheduler = new GridScheduler(tasks, allTools, schedule, activeFieldRepository, forecastService);
        List<PlannedTask> result = scheduler.compute();

        printSchedule(result);

        PlannedTask ptB = findPlannedTask(result, 2L);
        assertTrue(ptB.isConflicted(), "Task B sollte conflicted sein, da kein Platz und Verschieben unmöglich");
    }

    @Test
    void testChainReaction_DominoEffect() {
        // --- SETUP ---
        ToolCategory tractorCat = new ToolCategory();
        tractorCat.setId(1L);
        tractorCat.setName("Tractors");
        Tool tractor1 = createTool(1L, "Tractor 1", tractorCat); // NUR 1 TOOL!
        List<Tool> allTools = List.of(tractor1);

        // Task A (Der erste Stein): Flexible Zeit, Hohe Prio
        Task taskA = createTask(1L, "Task A (High)", 2, field1);
        taskA.setCategories(List.of(tractorCat));
        taskA.setWindowStart(baseTime); // ab 08:00
        taskA.setWindowEnd(baseTime.plusHours(4)); // MUSS 08:00-10:00 laufen!

        // Task B (Der zweite Stein): Flexible Zeit, Mittlere Prio
        Task taskB = createTask(2L, "Task B (Mid)", 2, field1);
        taskB.setCategories(List.of(tractorCat));
        taskB.setWindowStart(baseTime); // ab 08:00

        // Task C (Der Auslöser): Extrem enges Fenster, Niedrige Prio (kommt als letztes)
        Task taskC = createTask(3L, "Task C (Late & Strict)", 2, field1);
        taskC.setCategories(List.of(tractorCat));
        taskC.setWindowStart(baseTime);
        taskC.setWindowEnd(baseTime.plusHours(2)); // MUSS 08:00-10:00 laufen!

        // LIST ORDER MATTERS: Wir simulieren, dass A und B zuerst geplant werden.
        // In the real scheduler, this would happen via priorities.
        // We set the list by hand.
        // Hier: IDs 1, 2, 3.
        List<Task> tasks = List.of(taskA, taskB, taskC);

        GridScheduler scheduler = new GridScheduler(tasks, allTools, schedule, activeFieldRepository, forecastService);
        List<PlannedTask> result = scheduler.compute();

        printSchedule(result);

        // --- VERIFY ---
        PlannedTask ptC = findPlannedTask(result, 3L);
        PlannedTask ptA = findPlannedTask(result, 1L);
        PlannedTask ptB = findPlannedTask(result, 2L);

        // 1. C should be scheduled at its fixed time
        assertEquals(baseTime.plusHours(2), ptC.getAssignedEnd(), "Task C must be finished by 10:00");
        assertFalse(ptC.isConflicted());

        // 2. A muss would most probably be moved
        assertEquals(baseTime.plusHours(2), ptA.getAssignedStart(), "Task A sollte auf 10:00 geschoben worden sein");
        assertFalse(ptA.isConflicted());

        // 3. B muss NOCH WEITER verschoben worden sein
        assertEquals(baseTime.plusHours(4), ptB.getAssignedStart(), "Task B sollte auf 12:00 geschoben worden sein");
        assertFalse(ptB.isConflicted());
    }

    /**
     * SZENARIO: Multiple Blockers Displacement (1 vs N)
     * Eine große Task (4h) braucht zwingend den Slot 08:00 - 12:00.
     * Dieser Slot ist aber bereits von zwei kleinen Tasks (je 2h) belegt.
     * Der Scheduler muss BEIDE kleinen Tasks verschieben, um Platz für die große zu schaffen.
     */
    @Test
    void testMultipleBlockersDisplacement() {
        // --- SETUP ---
        // Nur 1 Traktor verfügbar -> Konflikt unvermeidbar ohne Verschieben
        ToolCategory tractorCat = new ToolCategory();
        tractorCat.setId(1L);
        tractorCat.setName("Tractors");
        Tool tractor1 = createTool(1L, "Tractor 1", tractorCat);
        List<Tool> allTools = List.of(tractor1);

        // Wir simulieren die "Besetzung" durch Listen-Reihenfolge.
        // Der Scheduler arbeitet die Liste ab. Wir tun so, als wären B und C zuerst dran
        // (z.B. weil sie höhere Prio-Scores haben).

        // Blocker 1: 2 Stunden (landet greedy auf 08:00 - 10:00)
        Task taskB = createTask(2L, "Blocker 1", 2, field1);
        taskB.setCategories(List.of(tractorCat));
        taskB.setWindowStart(baseTime);

        // Blocker 2: 2 Stunden (landet greedy auf 10:00 - 12:00)
        Task taskC = createTask(3L, "Blocker 2", 2, field1);
        taskC.setCategories(List.of(tractorCat));
        taskC.setWindowStart(baseTime);

        // The Big Task A: 4 Stunden.
        // CONSTRAINT: MUSS zwischen 08:00 und 12:00 laufen.
        // Wir setzen WindowEnd auf 12:00. Da Duration=4h, ist Startzeitpunkt 08:00 die EINZIGE Option.
        Task taskA = createTask(1L, "Big Task A", 4, field1);
        taskA.setCategories(List.of(tractorCat));
        taskA.setWindowStart(baseTime);       // ab 08:00
        taskA.setWindowEnd(baseTime.plusHours(4)); // bis 12:00

        // Reihenfolge in der Liste: B, C, A
        // Scheduler plant B (08-10), dann C (10-12).
        // Dann kommt A. A braucht 08-12. Findet B und C als Blocker.
        List<Task> tasks = List.of(taskB, taskC, taskA);

        GridScheduler scheduler = new GridScheduler(tasks, allTools, schedule, activeFieldRepository, forecastService);
        List<PlannedTask> result = scheduler.compute();

        printSchedule(result);

        // --- VERIFY ---
        PlannedTask ptA = findPlannedTask(result, 1L);
        PlannedTask ptB = findPlannedTask(result, 2L);
        PlannedTask ptC = findPlannedTask(result, 3L);

        // 1. Task A muss seinen Willen bekommen haben (08:00 - 12:00)
        assertEquals(baseTime, ptA.getAssignedStart(), "Task A (Big) muss um 08:00 starten");
        assertEquals(baseTime.plusHours(4), ptA.getAssignedEnd(), "Task A muss bis 12:00 gehen");
        assertFalse(ptA.isConflicted(), "Task A sollte konfliktfrei sein");

        // 2. Task B muss verschoben worden sein (war 08:00, muss jetzt >= 12:00 sein)
        assertTrue(ptB.getAssignedStart().isAfter(baseTime.plusMinutes(1)) || ptB.getAssignedStart().isEqual(baseTime.plusHours(4)),
            "Blocker B muss nach hinten verschoben worden sein (ab 12:00)");
        assertFalse(ptB.isConflicted());

        // 3. Task C muss auch verschoben worden sein
        assertTrue(ptC.getAssignedStart().isAfter(baseTime.plusMinutes(1)) || ptC.getAssignedStart().isEqual(baseTime.plusHours(4)),
            "Blocker C muss nach hinten verschoben worden sein");
        assertFalse(ptC.isConflicted());

        // 4. Keine Überlappungen (da nur 1 Tool)
        assertFalse(isOverlapping(ptA, ptB));
        assertFalse(isOverlapping(ptA, ptC));
        assertFalse(isOverlapping(ptB, ptC));
    }

    // --- HELPER METHODS ---

    private Task createTask(Long id, String name, float durationHours, Field field) {
        Task t = new Task();
        t.setId(id);
        t.setName(name);
        t.setWorkTimeEstimate(durationHours);
        t.setField(field);
        t.setOwner(owner);
        t.setFixed(false);
        // Default Window: 1 Jahr
        t.setWindowStart(baseTime.minusDays(1));
        t.setWindowEnd(baseTime.plusDays(365));
        return t;
    }

    private Task createTaskWithWindow(Long id, String name, float durationHours, Field field,
                                        LocalDateTime windowStart, LocalDateTime windowEnd) {
        Task t = createTask(id, name, durationHours, field);
        t.setWindowStart(windowStart);
        t.setWindowEnd(windowEnd);
        return t;
    }

    private Tool createTool(Long id, String name, ToolCategory cat) {
        Tool t = new Tool();
        t.setId(id);
        t.setName(name);
        t.setCategory(cat);
        t.setOwner(owner);
        t.setUsable(true);
        return t;
    }

    private PlannedTask findPlannedTask(List<PlannedTask> list, Long taskId) {
        return list.stream()
            .filter(pt -> pt.getTask().getId().equals(taskId))
            .findFirst()
            .orElseThrow(() -> new AssertionError("Task " + taskId + " not found in result"));
    }

    private boolean isOverlapping(PlannedTask t1, PlannedTask t2) {
        return t1.getAssignedStart().isBefore(t2.getAssignedEnd()) &&
            t2.getAssignedStart().isBefore(t1.getAssignedEnd());
    }

    // Neue Methode: prüft, dass kein Tool gleichzeitig für überlappende Tasks verwendet wird.
    private void assertNoToolConflicts(List<PlannedTask> tasks) {
        // Map ToolId -> List<PlannedTask> die dieses Tool verwenden
        java.util.Map<Long, List<PlannedTask>> byTool = new java.util.HashMap<>();

        for (PlannedTask pt : tasks) {
            if (pt.getFixedEquipment() == null) continue;
            for (Tool tool : pt.getFixedEquipment()) {
                if (tool == null || tool.getId() == null) continue;
                byTool.computeIfAbsent(tool.getId(), k -> new ArrayList<>()).add(pt);
            }
        }

        for (java.util.Map.Entry<Long, List<PlannedTask>> entry : byTool.entrySet()) {
            List<PlannedTask> list = entry.getValue();
            if (list.size() < 2) continue;

            // Sortiere nach Startzeit
            list.sort((a, b) -> a.getAssignedStart().compareTo(b.getAssignedStart()));

            // Prüfe benachbarte Tasks auf Überlappung
            for (int i = 0; i < list.size() - 1; i++) {
                PlannedTask a = list.get(i);
                for (int j = i + 1; j < list.size(); j++) {
                    PlannedTask b = list.get(j);

                    // Wenn einer der Tasks als conflicted markiert ist, ignoriere das Paar
                    if (a.isConflicted() || b.isConflicted()) continue;

                    if (isOverlapping(a, b)) {
                        String toolName = a.getFixedEquipment().stream()
                            .filter(t -> t != null && entry.getKey().equals(t.getId()))
                            .findFirst()
                            .map(Tool::getName)
                            .orElse("ToolId=" + entry.getKey());

                        String msg = String.format(
                            "Tool-Konflikt erkannt für Tool '%s' (id=%d): Task %d [%s - %s] überschneidet Task %d [%s - %s]",
                            toolName,
                            entry.getKey(),
                            a.getTask().getId(), a.getAssignedStart(), a.getAssignedEnd(),
                            b.getTask().getId(), b.getAssignedStart(), b.getAssignedEnd()
                        );
                        fail(msg);
                    }
                }
            }
        }
    }

    private void printSchedule(List<PlannedTask> tasks) {
        System.out.println("\n--- SCHEDULING RESULT ---");
        tasks.stream()
            .sorted((a, b) -> a.getAssignedStart().compareTo(b.getAssignedStart()))
            .forEach(pt -> {
                String tools = pt.getFixedEquipment().isEmpty() ? "No Tool" : pt.getFixedEquipment().get(0).getName();
                System.out.printf("[%s] %s - %s | Task: %s | Tool: %s | Conflicted: %s%n",
                    pt.getTask().getId(),
                    pt.getAssignedStart().toLocalTime(),
                    pt.getAssignedEnd().toLocalTime(),
                    pt.getTask().getName(),
                    tools,
                    pt.isConflicted() ? "YES" : "NO");
            });
        System.out.println("-------------------------\n");
    }

    @Test
    void testValueBasedReplacementBug_MaintenanceTasks() throws Exception {
        // --- ZEIT & FENSTER ---
        // Wir simulieren das Fenster 18:00 - 22:00 (4 Stunden)
        LocalDateTime windowStart = baseTime.withHour(18).withMinute(0);
        LocalDateTime windowEnd = windowStart.plusHours(4); // bis 22:00

        // --- TOOLS ---
        ToolCategory axeCat = new ToolCategory();
        axeCat.setId(10L);
        axeCat.setName("Axes");

        Tool axe1 = createTool(1L, "Axe John", axeCat);
        List<Tool> allTools = List.of(axe1); // Nur 1 Axt verfügbar -> Konflikt!

        // --- TASKS ---

        // 1. BigTask: 4 Stunden, braucht Axt, Prio ~409k
        // Passt genau in das Fenster (18-22 Uhr)
        Task bigTask = createTask(1L, "BigTask Profit Maintenance", 4.0f, field1);
        bigTask.setTaskType(TaskType.MAINTENANCE);
        //bigTask.setCategories(List.of(axeCat)); // Oder specific tool, wenn gewünscht
        bigTask.setTools(List.of(axe1)); // Specific Tool Requirement
        bigTask.setWindowStart(windowStart);
        bigTask.setWindowEnd(windowEnd);

        // 2. SmallTask 1: 2 Stunden, braucht Axt, Prio ~330k
        Task smallTask1 = createTask(2L, "SmallTask 1 Profit Maintenance", 2.0f, field1);
        smallTask1.setTaskType(TaskType.MAINTENANCE);
        //smallTask1.setCategories(List.of(axeCat));
        smallTask1.setTools(List.of(axe1));
        smallTask1.setWindowStart(windowStart);
        smallTask1.setWindowEnd(windowEnd);

        // 3. SmallTask 2: 2 Stunden, braucht Axt, Prio ~330k
        Task smallTask2 = createTask(3L, "SmallTask 2 Profit Maintenance", 2.0f, field1);
        smallTask2.setTaskType(TaskType.MAINTENANCE);
        //smallTask2.setCategories(List.of(axeCat));
        smallTask2.setTools(List.of(axe1)); // Specific Tool Requirement
        smallTask2.setWindowStart(windowStart);
        smallTask2.setWindowEnd(windowEnd);

        List<Task> tasks = List.of(bigTask, smallTask1, smallTask2);

        // --- SCHEDULER SETUP ---
        GridScheduler scheduler = new GridScheduler(tasks, allTools, schedule, activeFieldRepository, forecastService);

        // --- MOCKING PRIORITY SCORER (via Reflection) ---

        PriorityScorer mockScorer = org.mockito.Mockito.mock(PriorityScorer.class);

        // Scores setzen wie in deiner Beschreibung
        lenient().when(mockScorer.score(org.mockito.ArgumentMatchers.eq(bigTask), any())).thenReturn(409789L);
        lenient().when(mockScorer.score(org.mockito.ArgumentMatchers.eq(smallTask1), any())).thenReturn(330718L);
        lenient().when(mockScorer.score(org.mockito.ArgumentMatchers.eq(smallTask2), any())).thenReturn(330718L);

        // Injektion via Reflection
        java.lang.reflect.Field scorerField = GridScheduler.class.getDeclaredField("priorityScorer");
        scorerField.setAccessible(true);
        scorerField.set(scheduler, mockScorer);

        // --- EXECUTE ---
        List<PlannedTask> result = scheduler.compute();

        // --- VERIFY ---
        printSchedule(result);

        // Assertions für Debugging:
        PlannedTask ptBig = findPlannedTask(result, 1L);
        PlannedTask ptSmall1 = findPlannedTask(result, 2L);
        PlannedTask ptSmall2 = findPlannedTask(result, 3L);

        // Prüfen auf Überlappungen (Das ist der Bug, den du vermutest)
        boolean overlap1 = !ptBig.isConflicted() && !ptSmall1.isConflicted() && isOverlapping(ptBig, ptSmall1);
        boolean overlap2 = !ptBig.isConflicted() && !ptSmall2.isConflicted() && isOverlapping(ptBig, ptSmall2);
        boolean overlapSmall = !ptSmall1.isConflicted() && !ptSmall2.isConflicted() && isOverlapping(ptSmall1, ptSmall2);

        if (overlap1 || overlap2 || overlapSmall) {
            System.err.println("!!! BUG DETECTED: OVERLAPPING TASKS ON SAME TOOL !!!");
        }

        assertFalse(overlap1, "BigTask und SmallTask 1 dürfen sich nicht überlappen");
        assertFalse(overlap2, "BigTask und SmallTask 2 dürfen sich nicht überlappen");
        assertFalse(overlapSmall, "SmallTask 1 und SmallTask 2 dürfen sich nicht überlappen");
    }

    @Test
    void testSimpleValueSwapDebug() throws Exception {
        LocalDateTime windowStart = baseTime.withHour(18).withMinute(0);
        LocalDateTime windowEnd = windowStart.plusHours(4);

        ToolCategory axeCat = new ToolCategory();
        axeCat.setId(1L);
        axeCat.setName("Axes");
        Tool axe1 = createTool(1L, "Axe John", axeCat);
        List<Tool> allTools = List.of(axe1);

        Task bigTask = createTask(1L, "BigTask", 4.0f, field1);
        bigTask.setTaskType(TaskType.MAINTENANCE);
        bigTask.setTools(List.of(axe1));
        bigTask.setWindowStart(windowStart);
        bigTask.setWindowEnd(windowEnd);

        Task smallTask1 = createTask(2L, "SmallTask1", 2.0f, field1);
        smallTask1.setTaskType(TaskType.MAINTENANCE);
        smallTask1.setTools(List.of(axe1));
        smallTask1.setWindowStart(windowStart);
        smallTask1.setWindowEnd(windowEnd);

        Task smallTask2 = createTask(3L, "SmallTask2", 2.0f, field1);
        smallTask2.setTaskType(TaskType.MAINTENANCE);
        smallTask2.setTools(List.of(axe1));
        smallTask2.setWindowStart(windowStart);
        smallTask2.setWindowEnd(windowEnd);

        // Nur zwei Tasks für einfaches Debugging
        List<Task> tasks = List.of(smallTask1, bigTask, smallTask2);

        GridScheduler scheduler = new GridScheduler(tasks, allTools, schedule, activeFieldRepository, forecastService);

        // Mocking...
        PriorityScorer mockScorer = org.mockito.Mockito.mock(PriorityScorer.class);

        // Scores setzen wie in deiner Beschreibung
        lenient().when(mockScorer.score(org.mockito.ArgumentMatchers.eq(bigTask), any())).thenReturn(409789L);
        lenient().when(mockScorer.score(org.mockito.ArgumentMatchers.eq(smallTask1), any())).thenReturn(330718L);
        lenient().when(mockScorer.score(org.mockito.ArgumentMatchers.eq(smallTask2), any())).thenReturn(330718L);

        // Injektion via Reflection
        java.lang.reflect.Field scorerField = GridScheduler.class.getDeclaredField("priorityScorer");
        scorerField.setAccessible(true);
        scorerField.set(scheduler, mockScorer);

        List<PlannedTask> result = scheduler.compute();

        printSchedule(result);

        // Assertion: Keine überlappenden Bookings auf demselben Tool
        assertNoToolConflicts(result);

        // Assertions für Debugging:
        PlannedTask ptBig = findPlannedTask(result, 1L);
        PlannedTask ptSmall1 = findPlannedTask(result, 2L);
        PlannedTask ptSmall2 = findPlannedTask(result, 3L);

        // Prüfen auf Überlappungen (Das ist der Bug, den du vermutest)
        boolean overlap1 = !ptBig.isConflicted() && !ptSmall1.isConflicted() && isOverlapping(ptBig, ptSmall1);
        boolean overlap2 = !ptBig.isConflicted() && !ptSmall2.isConflicted() && isOverlapping(ptBig, ptSmall2);
        boolean overlapSmall = !ptSmall1.isConflicted() && !ptSmall2.isConflicted() && isOverlapping(ptSmall1, ptSmall2);

        if (overlap1 || overlap2 || overlapSmall) {
            System.err.println("!!! BUG DETECTED: OVERLAPPING TASKS ON SAME TOOL !!!");
        }

        assertFalse(overlap1, "BigTask und SmallTask 1 dürfen sich nicht überlappen");
        assertFalse(overlap2, "BigTask und SmallTask 2 dürfen sich nicht überlappen");
        assertFalse(overlapSmall, "SmallTask 1 und SmallTask 2 dürfen sich nicht überlappen");
    }

    /**
     * SZENARIO: Value-Based Swap mit "Exact Fit"
     * Zeitfenster: 8 Stunden (14:00 - 22:00).
     * Tool: 1 Axt verfügbar.
     *
     * Tasks:
     * 1. BigTask (6h):   Passt rein, lässt aber nur 2h übrig.
     * 2. SmallTask1 (4h): Passt alleine rein.
     * 3. SmallTask2 (4h): Passt alleine rein.
     *
     * Konflikt:
     * - Wenn BigTask (6h) geplant ist, passen SmallTask1 (4h) und SmallTask2 (4h) nicht mehr rein (Rest 2h < 4h).
     * - Zusammen haben die SmallTasks aber 8h Arbeitszeit und (angenommen) einen höheren Gesamtwert.
     *
     * Erwartung:
     * Der Scheduler entfernt BigTask und plant stattdessen SmallTask1 UND SmallTask2 ein,
     * da diese das 8h Fenster perfekt ausfüllen (4h + 4h = 8h) und der Gesamtwert höher ist.
     */
    @Test
    void testValueSwap_BigVsTwoSmall_ExactFit() throws Exception {
        // --- ZEIT & FENSTER ---
        // Fenster: 14:00 bis 22:00 (8 Stunden)
        LocalDateTime windowStart = baseTime.plusDays(1).withHour(14).withMinute(0);
        LocalDateTime windowEnd = windowStart.plusHours(8); // 22:00

        // --- TOOLS ---
        ToolCategory axeCat = new ToolCategory();
        axeCat.setId(20L);
        axeCat.setName("Axes");

        Tool axe1 = createTool(1L, "Axe John", axeCat);
        List<Tool> allTools = List.of(axe1); // Nur 1 Axt!

        // --- FIELDS ---
        // Die Tasks nutzen unterschiedliche Felder, also kein Feld-Konflikt, nur Tool-Konflikt.
        Field fieldBig = new Field(); fieldBig.setId(300L); fieldBig.setName("Field 3 John");
        Field fieldSmall = new Field(); fieldSmall.setId(200L); fieldSmall.setName("Field 2 John");

        // --- TASKS ---

        // 1. BigTask: 6 Stunden
        Task bigTask = createTask(1L, "BigTask Profit Maintenance", 6.0f, fieldBig);
        bigTask.setTaskType(TaskType.MAINTENANCE);
        bigTask.setTools(List.of(axe1));
        bigTask.setWindowStart(windowStart);
        bigTask.setWindowEnd(windowEnd);

        // 2. SmallTask 1: 4 Stunden
        Task smallTask1 = createTask(2L, "SmallTask 1 Profit Maintenance", 4.0f, fieldSmall);
        smallTask1.setTaskType(TaskType.MAINTENANCE);
        smallTask1.setTools(List.of(axe1));
        smallTask1.setWindowStart(windowStart);
        smallTask1.setWindowEnd(windowEnd);

        // 3. SmallTask 2: 4 Stunden
        Task smallTask2 = createTask(3L, "SmallTask 2 Profit Maintenance", 4.0f, fieldSmall);
        smallTask2.setTaskType(TaskType.MAINTENANCE);
        smallTask2.setTools(List.of(axe1));
        smallTask2.setWindowStart(windowStart);
        smallTask2.setWindowEnd(windowEnd);

        // Liste so sortieren, dass BigTask zuerst kommt (simuliert durch hohe Prio oder Listenposition)
        List<Task> tasks = List.of(bigTask, smallTask1, smallTask2);

        GridScheduler scheduler = new GridScheduler(tasks, allTools, schedule, activeFieldRepository, forecastService);

        // --- MOCKING PRIORITY SCORER ---
        PriorityScorer mockScorer = org.mockito.Mockito.mock(PriorityScorer.class);

        // Wir definieren Scores, so dass:
        // Score(Big) > Score(Small)  -> Big wird zuerst gewählt / ist "wertvoller" als eine einzelne SmallTask
        // Score(Big) < 2 * Score(Small) -> Aber zwei SmallTasks sind wertvoller als BigTask
        //
        // Value = Score * Duration
        // Value(Big)    = 100 * 6 = 600
        // Value(Small)  =  80 * 4 = 320
        // Summe Small   = 320 + 320 = 640
        //
        // Delta = -600 (Big raus) + 320 (Small1 rein) + 320 (Small2 rein) = +40 -> SWAP!

        lenient().when(mockScorer.score(org.mockito.ArgumentMatchers.eq(bigTask), any())).thenReturn(100L);
        lenient().when(mockScorer.score(org.mockito.ArgumentMatchers.eq(smallTask1), any())).thenReturn(80L);
        lenient().when(mockScorer.score(org.mockito.ArgumentMatchers.eq(smallTask2), any())).thenReturn(80L);

        // Reflection Injection des Mock Scorers
        java.lang.reflect.Field scorerField = GridScheduler.class.getDeclaredField("priorityScorer");
        scorerField.setAccessible(true);
        scorerField.set(scheduler, mockScorer);

        // --- EXECUTE ---
        List<PlannedTask> result = scheduler.compute();

        // --- VERIFY ---
        printSchedule(result);

        PlannedTask ptBig = findPlannedTask(result, 1L);
        PlannedTask ptSmall1 = findPlannedTask(result, 2L);
        PlannedTask ptSmall2 = findPlannedTask(result, 3L);

        // 1. Die beiden kleinen Tasks sollten eingeplant sein (Conflicted: NO)
        assertFalse(ptSmall1.isConflicted(), "SmallTask 1 sollte erfolgreich eingeplant sein");
        assertFalse(ptSmall2.isConflicted(), "SmallTask 2 sollte erfolgreich eingeplant sein");

        // 2. Die große Task muss weichen (Conflicted: YES), weil der Gesamt-Wert der kleinen höher ist
        assertTrue(ptBig.isConflicted(), "BigTask sollte zugunsten der beiden kleineren Tasks verdrängt worden sein");

        // 3. Zeit-Check: Die beiden kleinen Tasks müssen das Fenster komplett füllen
        // Da nur 1 Tool da ist, müssen sie hintereinander liegen.
        // Mögliche Reihenfolge: S1 -> S2 oder S2 -> S1.
        boolean sequence1 = ptSmall1.getAssignedEnd().isEqual(ptSmall2.getAssignedStart());
        boolean sequence2 = ptSmall2.getAssignedEnd().isEqual(ptSmall1.getAssignedStart());

        assertTrue(sequence1 || sequence2, "Die SmallTasks sollten nahtlos hintereinander liegen (4h + 4h = 8h)");

        // 4. Keine Überlappung (Sicherheitscheck)
        assertFalse(isOverlapping(ptSmall1, ptSmall2), "SmallTasks dürfen sich nicht überlappen");
    }
}
