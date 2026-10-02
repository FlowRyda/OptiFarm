package at.ac.tuwien.ase.groupphase.backend.integrationtest;

import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.schedule.TaskDto;
import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.ToolCategoryDto;
import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.ToolDto;
import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.field.FieldDetailDto;
import at.ac.tuwien.ase.groupphase.backend.integrationtest.steps.task.CreateTaskPrerequisitesStep;
import at.ac.tuwien.ase.groupphase.backend.integrationtest.steps.task.CreateTaskStep;
import at.ac.tuwien.ase.groupphase.backend.integrationtest.steps.task.DeleteTaskStep;
import at.ac.tuwien.ase.groupphase.backend.integrationtest.steps.task.EditTaskInput;
import at.ac.tuwien.ase.groupphase.backend.integrationtest.steps.task.EditTaskStep;
import at.ac.tuwien.ase.groupphase.backend.integrationtest.steps.task.GetAllTasksStep;
import at.ac.tuwien.ase.groupphase.backend.integrationtest.steps.task.GetTaskStep;
import at.ac.tuwien.ase.groupphase.backend.integrationtest.steps.task.TaskPrerequisitesDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class TaskTests extends OptiFarmBaseTest {

    @Autowired
    private CreateTaskStep createTaskStep;
    @Autowired
    private GetTaskStep getTaskStep;
    @Autowired
    private GetAllTasksStep getAllTasksStep;
    @Autowired
    private EditTaskStep editTaskStep;
    @Autowired
    private DeleteTaskStep deleteTaskStep;
    @Autowired
    private CreateTaskPrerequisitesStep createTaskPrerequisitesStep;

    @Test
    void createTask_returns200_andTask() throws Exception {
        TaskPrerequisitesDto prereq = loginAndCreatePrereq();
        TaskDto newTask = createTestTask(prereq);

        TaskDto created = createTaskStep.run(newTask, status().isOk());

        assertNotNull(created);
        assertNotNull(created.getId());

        FieldDetailDto expectedField = prereq.field();
        ToolCategoryDto expectedCategory = prereq.category();

        assertAll(
            () -> assertEquals(newTask.getName(), created.getName(), "name"),
            () -> assertEquals(newTask.getWindowStart(), created.getWindowStart(), "windowStart"),
            () -> assertEquals(newTask.getWindowEnd(), created.getWindowEnd(), "windowEnd"),
            () -> assertEquals(newTask.getWorkTimeEstimate(), created.getWorkTimeEstimate(), 1e-6, "workTimeEstimate"),

            () -> assertNotNull(created.getField(), "field should not be null"),
            () -> assertEquals(expectedField.id(), created.getField().id(), "field id"),

            () -> assertTrue(created.getTools() == null || created.getTools().isEmpty(),
                "tools should be empty for category-based task"),

            () -> assertNotNull(created.getCategories(), "categories should not be null"),
            () -> assertEquals(1, created.getCategories().size(), "categories size"),
            () -> assertEquals(expectedCategory.getId(),
                created.getCategories().get(0).getId(),
                "category id")
        );
    }

    @Test
    void getTask_returnsTaskById() throws Exception {
        TaskPrerequisitesDto prereq = loginAndCreatePrereq();
        TaskDto createdTask = createTestTask(prereq);

        TaskDto created = createTaskStep.run(createdTask, status().isOk());

        TaskDto fetched = getTaskStep.run(created.getId(), status().isOk());

        assertNotNull(fetched);
        assertEquals(created.getId(), fetched.getId());

        assertAll(
            () -> assertEquals(created.getName(), fetched.getName(), "name"),
            () -> assertEquals(created.getWindowStart(), fetched.getWindowStart(), "windowStart"),
            () -> assertEquals(created.getWindowEnd(), fetched.getWindowEnd(), "windowEnd"),
            () -> assertEquals(created.getWorkTimeEstimate(), fetched.getWorkTimeEstimate(), 1e-6, "workTimeEstimate"),

            () -> {
                assertNotNull(fetched.getField(), "field should not be null");
                assertEquals(created.getField().id(), fetched.getField().id(), "field id");
            },

            () -> assertTrue(fetched.getTools() == null || fetched.getTools().isEmpty(),
                "tools should be empty for category-based task"),

            () -> {
                assertNotNull(fetched.getCategories(), "categories should not be null");
                assertEquals(created.getCategories().size(), fetched.getCategories().size(), "categories size");
                for (int i = 0; i < created.getCategories().size(); i++) {
                    assertEquals(created.getCategories().get(i).getId(),
                        fetched.getCategories().get(i).getId(),
                        "category id at index " + i);
                }
            }
        );
    }

    @Test
    void getAllTasks_containsCreatedTask() throws Exception {
        TaskPrerequisitesDto prereq = loginAndCreatePrereq();
        TaskDto createdTask = createTestTask(prereq);

        TaskDto created = createTaskStep.run(createdTask, status().isOk());

        List<TaskDto> all = getAllTasksStep.run(null, status().isOk());

        assertNotNull(all);
        assertFalse(all.isEmpty());
        assertTrue(all.stream().anyMatch(t -> t.getId().equals(created.getId())));
    }

    @Test
    void updateTask_updatesData() throws Exception {
        TaskPrerequisitesDto prereq = loginAndCreatePrereq();
        TaskDto createdTask = createTestTask(prereq);

        TaskDto created = createTaskStep.run(createdTask, status().isOk());

        TaskDto updatedDto = createUpdatedTask(created, prereq);

        EditTaskInput input = new EditTaskInput(created.getId(), updatedDto);

        TaskDto updated = editTaskStep.run(input, status().isOk());

        assertNotNull(updated);
        assertEquals(created.getId(), updated.getId());

        assertAll(
            () -> assertEquals(created.getId(), updated.getId(), "id"),

            () -> assertEquals(updatedDto.getName(), updated.getName(), "name"),
            () -> assertEquals(updatedDto.getWindowStart(), updated.getWindowStart(), "windowStart"),
            () -> assertEquals(updatedDto.getWindowEnd(), updated.getWindowEnd(), "windowEnd"),
            () -> assertEquals(updatedDto.getWorkTimeEstimate(), updated.getWorkTimeEstimate(), 1e-6, "workTimeEstimate"),

            () -> {
                assertNotNull(updated.getField(), "field should not be null");
                assertEquals(updatedDto.getField().id(), updated.getField().id(), "field id");
            },

            () -> {
                assertNotNull(updated.getTools(), "tools should not be null");
                assertNotNull(updated.getCategories(), "Categories should not be null");
            },

            () -> assertTrue(
                !updated.getTools().isEmpty() || !updated.getCategories().isEmpty(),
                "Either tools or categories must contain at least one element"
            )
        );
    }

    @Test
    void deleteTask_returnsTrueAndRemovesTask() throws Exception {
        TaskPrerequisitesDto prereq = loginAndCreatePrereq();
        TaskDto createdTask = createTestTask(prereq);

        TaskDto created = createTaskStep.run(createdTask, status().isOk());

        Boolean deleted = deleteTaskStep.run(created.getId(), status().isOk());

        assertNotNull(deleted);
        assertTrue(deleted);

        getTaskStep.run(created.getId(), status().isNotFound());
    }

    @Test
    void createTask_withInvalidData_returns400() throws Exception {
        TaskPrerequisitesDto prereq = loginAndCreatePrereq();
        TaskDto invalid = createTestTask(prereq);
        invalid.setName(null);

        TaskDto response = createTaskStep.run(invalid, status().isBadRequest());

        assertNull(response);
    }

    @Test
    void createTask_withoutToolsAndCategories_returns400() throws Exception {
        TaskPrerequisitesDto prereq = loginAndCreatePrereq();
        TaskDto dto = createTestTask(prereq);

        dto.setTools(null);
        dto.setCategories(null);

        TaskDto response = createTaskStep.run(dto, status().isBadRequest());
        assertNull(response);
    }

    @Test
    void createTask_withTooLargeWorkTime_returns400() throws Exception {
        TaskPrerequisitesDto prereq = loginAndCreatePrereq();
        TaskDto dto = createTestTask(prereq);

        dto.setWindowStart(LocalDateTime.of(2030, 1, 1, 8, 0));
        dto.setWindowEnd(LocalDateTime.of(2030, 1, 1, 11, 0));
        dto.setWorkTimeEstimate(4.0f);

        TaskDto response = createTaskStep.run(dto, status().isBadRequest());
        assertNull(response);
    }

    @Test
    void createTask_withZeroWorkTime_returns400() throws Exception {
        TaskPrerequisitesDto prereq = loginAndCreatePrereq();
        TaskDto dto = createTestTask(prereq);

        dto.setWorkTimeEstimate(0.0f);

        TaskDto response = createTaskStep.run(dto, status().isBadRequest());
        assertNull(response);
    }

    @Test
    void createTask_withToolWithoutId_returns400() throws Exception {
        TaskPrerequisitesDto prereq = loginAndCreatePrereq();
        TaskDto dto = createTestTask(prereq);

        ToolDto toolWithoutId = new ToolDto();
        toolWithoutId.setId(null);
        dto.setTools(List.of(toolWithoutId));
        dto.setCategories(null);

        TaskDto response = createTaskStep.run(dto, status().isBadRequest());
        assertNull(response);
    }

    private TaskPrerequisitesDto loginAndCreatePrereq() throws Exception {
        loginWithOk().run(loginDto);
        return createTaskPrerequisitesStep.create();
    }

    private TaskDto createTestTask(TaskPrerequisitesDto prereq) {
        TaskDto dto = new TaskDto();

        dto.setId(null);
        dto.setName("Test Task");

        dto.setWindowStart(LocalDateTime.of(2030, 1, 1, 8, 0));
        dto.setWindowEnd(LocalDateTime.of(2030, 1, 1, 11, 0));
        dto.setWorkTimeEstimate(2.5f);

        dto.setField(prereq.field());

        dto.setTools(new ArrayList<>());
        dto.setCategories(List.of(prereq.category()));

        return dto;
    }

    private TaskDto createUpdatedTask(TaskDto existing, TaskPrerequisitesDto prereq) {
        TaskDto updated = new TaskDto();

        updated.setId(existing.getId());
        updated.setName("Updated Task");
        updated.setWindowStart(LocalDateTime.of(2030, 1, 2, 9, 0));
        updated.setWindowEnd(LocalDateTime.of(2030, 1, 2, 12, 0));
        updated.setWorkTimeEstimate(3.0f);

        updated.setField(prereq.field());

        updated.setTools(List.of(prereq.tool()));
        updated.setCategories(new ArrayList<>());

        return updated;
    }
}
