package at.ac.tuwien.ase.groupphase.backend.integrationtest;

import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.schedule.PlannedTaskDto;
import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.schedule.ScheduleDto;
import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.schedule.TaskDto;
import at.ac.tuwien.ase.groupphase.backend.integrationtest.steps.schedule.CreateScheduleStep;
import at.ac.tuwien.ase.groupphase.backend.integrationtest.steps.schedule.DeleteScheduleStep;
import at.ac.tuwien.ase.groupphase.backend.integrationtest.steps.schedule.GetAllSchedulesStep;
import at.ac.tuwien.ase.groupphase.backend.integrationtest.steps.schedule.GetScheduleStep;
import at.ac.tuwien.ase.groupphase.backend.integrationtest.steps.task.CreateTaskPrerequisitesStep;
import at.ac.tuwien.ase.groupphase.backend.integrationtest.steps.task.CreateTaskStep;
import at.ac.tuwien.ase.groupphase.backend.integrationtest.steps.task.TaskPrerequisitesDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ScheduleTests extends OptiFarmBaseTest {

    @Autowired private CreateScheduleStep createScheduleStep;
    @Autowired private GetScheduleStep getScheduleStep;
    @Autowired private GetAllSchedulesStep getAllSchedulesStep;
    @Autowired private DeleteScheduleStep deleteScheduleStep;

    @Autowired private CreateTaskPrerequisitesStep createTaskPrerequisitesStep;
    @Autowired private CreateTaskStep createTaskStep;

    @Test
    void createSchedule_returns200_andSchedule() throws Exception {
        TaskPrerequisitesDto prereq = loginAndCreatePrereq();

        List<TaskDto> createdTasks = createSomeValidTasks(prereq);

        ScheduleDto created = createScheduleStep.run(null, status().isOk());

        assertNotNull(created);
        assertNotNull(created.getId());

        assertNotNull(created.getPlannedTasks(), "plannedTasks should not be null");

        var taskIds = created.getPlannedTasks().stream()
            .map(PlannedTaskDto::getTaskId)
            .toList();

        assertEquals(taskIds.size(), taskIds.stream().distinct().count(),
            "Each taskId should appear at most once in plannedTasks");

        var createdTaskIds = createdTasks.stream().map(TaskDto::getId).collect(java.util.stream.Collectors.toSet());

        assertTrue(created.getPlannedTasks().stream().allMatch(pt -> createdTaskIds.contains(pt.getTaskId())),
            "plannedTasks must only reference tasks owned/created in this test setup");


        for (PlannedTaskDto pt : created.getPlannedTasks()) {
            assertNotNull(pt.getTaskId(), "planned task must reference original taskId");
            assertNotNull(pt.getWindowStart(), "planned task windowStart should be present");
            assertNotNull(pt.getWindowEnd(), "planned task windowEnd should be present");
        }
    }

    @Test
    void getSchedule_returnsScheduleById() throws Exception {
        TaskPrerequisitesDto prereq = loginAndCreatePrereq();
        List<TaskDto> createdTasks = createSomeValidTasks(prereq);

        ScheduleDto created = createScheduleStep.run(null, status().isOk());
        ScheduleDto fetched = getScheduleStep.run(created.getId(), status().isOk());

        assertNotNull(fetched);
        assertEquals(created.getId(), fetched.getId());

        assertNotNull(fetched.getPlannedTasks(), "plannedTasks should not be null");
    }

    @Test
    void getAllSchedules_containsCreatedSchedule() throws Exception {
        TaskPrerequisitesDto prereq = loginAndCreatePrereq();
        List<TaskDto> createdTasks = createSomeValidTasks(prereq);

        ScheduleDto created = createScheduleStep.run(null, status().isOk());

        List<ScheduleDto> all = getAllSchedulesStep.run(null, status().isOk());

        assertNotNull(all);
        assertTrue(all.stream().anyMatch(s -> s.getId().equals(created.getId())));
    }

    @Test
    void deleteSchedule_removesSchedule() throws Exception {
        TaskPrerequisitesDto prereq = loginAndCreatePrereq();
        List<TaskDto> createdTasks = createSomeValidTasks(prereq);

        ScheduleDto created = createScheduleStep.run(null, status().isOk());

        deleteScheduleStep.run(created.getId(), status().isOk());

        getScheduleStep.run(created.getId(), status().isNotFound());
    }

    private TaskPrerequisitesDto loginAndCreatePrereq() throws Exception {
        loginWithOk().run(loginDto);
        return createTaskPrerequisitesStep.create();
    }

    @Test
    void createSchedule_twice_createsTwoDifferentSchedules() throws Exception {
        TaskPrerequisitesDto prereq = loginAndCreatePrereq();
        createSomeValidTasks(prereq);

        ScheduleDto s1 = createScheduleStep.run(null, status().isOk());
        ScheduleDto s2 = createScheduleStep.run(null, status().isOk());

        assertNotNull(s1.getId());
        assertNotNull(s2.getId());
        assertNotEquals(s1.getId(), s2.getId(), "Each createSchedule call should create a new schedule row");
    }

    @Test
    void deleteSchedule_removesFromGetAll() throws Exception {
        TaskPrerequisitesDto prereq = loginAndCreatePrereq();
        createSomeValidTasks(prereq);

        ScheduleDto created = createScheduleStep.run(null, status().isOk());
        deleteScheduleStep.run(created.getId(), status().isOk());

        List<ScheduleDto> all = getAllSchedulesStep.run(null, status().isOk());
        assertTrue(all.stream().noneMatch(s -> s.getId().equals(created.getId())),
            "Deleted schedule should not appear in getAll");
    }

    private List<TaskDto> createSomeValidTasks(TaskPrerequisitesDto prereq) throws Exception {
        TaskDto t1 = createTaskStep.run(createCategoryTask(prereq, "T1", 2030, 1, 1, 8, 0, 11, 0, 2.0f), status().isOk());
        TaskDto t2 = createTaskStep.run(createCategoryTask(prereq, "T2", 2030, 1, 1, 12, 0, 15, 0, 2.0f), status().isOk());
        TaskDto t3 = createTaskStep.run(createCategoryTask(prereq, "T3", 2030, 1, 2, 8, 0, 12, 0, 3.0f), status().isOk());

        assertNotNull(t1); assertNotNull(t1.getId());
        assertNotNull(t2); assertNotNull(t2.getId());
        assertNotNull(t3); assertNotNull(t3.getId());

        return List.of(t1, t2, t3);
    }

    private TaskDto createCategoryTask(TaskPrerequisitesDto prereq,
                                       String name,
                                       int y, int mo, int d,
                                       int sh, int sm,
                                       int eh, int em,
                                       float workTimeHours) {
        TaskDto dto = new TaskDto();
        dto.setId(null);
        dto.setName(name);

        dto.setField(prereq.field());
        dto.setTools(List.of());
        dto.setCategories(List.of(prereq.category()));

        dto.setWindowStart(LocalDateTime.of(y, mo, d, sh, sm));
        dto.setWindowEnd(LocalDateTime.of(y, mo, d, eh, em));
        dto.setWorkTimeEstimate(workTimeHours);

        dto.setAssignedStart(null);
        dto.setAssignedEnd(null);

        return dto;
    }
}
