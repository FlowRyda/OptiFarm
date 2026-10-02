package at.ac.tuwien.ase.groupphase.backend.endpoint;

import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.schedule.TaskDto;
import at.ac.tuwien.ase.groupphase.backend.exception.AccessDeniedException;
import at.ac.tuwien.ase.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.ase.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.ase.groupphase.backend.service.TaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.lang.invoke.MethodHandles;
import java.util.List;

@RestController
@RequestMapping(value = "api/v1/task")
public class TaskEndpoint {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final TaskService taskService;

    @Autowired
    public TaskEndpoint(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping
    public ResponseEntity<TaskDto> createTask(@RequestBody TaskDto taskDto) throws ValidationException {
        LOGGER.trace("Creating Task: {}", taskDto);
        return ResponseEntity.ok(taskService.createTask(taskDto));
    }

    @GetMapping
    public ResponseEntity<List<TaskDto>> getAllTasks() {
        LOGGER.trace("Retrieving all Tasks");
        return ResponseEntity.ok(taskService.getAllTasks());
    }

    @GetMapping("{id}")
    public ResponseEntity<TaskDto> getTask(@PathVariable("id") Long id) throws NotFoundException, AccessDeniedException {
        LOGGER.trace("Getting task with id {}", id);
        return ResponseEntity.ok(taskService.getTaskById(id));
    }

    @PutMapping("{id}")
    public ResponseEntity<TaskDto> updateTask(@PathVariable("id") Long id, @RequestBody TaskDto taskDto) throws ValidationException, NotFoundException, AccessDeniedException {
        LOGGER.trace("Updating Task with id {}: {}", id, taskDto);
        return ResponseEntity.ok(taskService.updateTask(id, taskDto));
    }

    @DeleteMapping("{id}")
    public boolean deleteTask(@PathVariable("id") Long id) throws NotFoundException, AccessDeniedException {
        LOGGER.trace("Deleting Task with id {}", id);
        taskService.deleteTask(id);
        return true;
    }
}
