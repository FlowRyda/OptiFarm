package at.ac.tuwien.ase.groupphase.backend.service.impl;

import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.schedule.TaskDto;
import at.ac.tuwien.ase.groupphase.backend.endpoint.mapper.TaskMapper;
import at.ac.tuwien.ase.groupphase.backend.entity.*;
import at.ac.tuwien.ase.groupphase.backend.exception.AccessDeniedException;
import at.ac.tuwien.ase.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.ase.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.ase.groupphase.backend.repository.FarmerRepository;
import at.ac.tuwien.ase.groupphase.backend.repository.FieldRepository;
import at.ac.tuwien.ase.groupphase.backend.repository.PlannedTaskRepository;
import at.ac.tuwien.ase.groupphase.backend.repository.TaskRepository;
import at.ac.tuwien.ase.groupphase.backend.repository.ToolCategoryRepository;
import at.ac.tuwien.ase.groupphase.backend.repository.ToolRepository;
import at.ac.tuwien.ase.groupphase.backend.security.JwtUtil;
import at.ac.tuwien.ase.groupphase.backend.service.TaskService;
import at.ac.tuwien.ase.groupphase.backend.service.validation.TaskValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TaskServiceImpl implements TaskService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final TaskRepository taskRepository;
    private final FarmerRepository farmerRepository;
    private final FieldRepository fieldRepository;
    private final ToolRepository toolRepository;
    private final ToolCategoryRepository toolCategoryRepository;
    private final TaskValidator taskValidator;
    private final TaskMapper taskMapper;
    private final PlannedTaskRepository plannedTaskRepository;

    @Autowired
    public TaskServiceImpl(TaskRepository taskRepository,
                           FarmerRepository farmerRepository,
                           FieldRepository fieldRepository,
                           ToolRepository toolRepository,
                           ToolCategoryRepository toolCategoryRepository,
                           TaskValidator taskValidator,
                           TaskMapper taskMapper,
                           PlannedTaskRepository plannedTaskRepository) {
        this.taskRepository = taskRepository;
        this.farmerRepository = farmerRepository;
        this.fieldRepository = fieldRepository;
        this.toolRepository = toolRepository;
        this.toolCategoryRepository = toolCategoryRepository;
        this.taskValidator = taskValidator;
        this.taskMapper = taskMapper;
        this.plannedTaskRepository = plannedTaskRepository;
    }

    @Override
    @Transactional
    public TaskDto createTask(TaskDto taskDto) throws ValidationException {
        LOGGER.info("Creating task with dto: {}", taskDto);

        var farmer = getFarmer();
        taskValidator.validateForCreate(taskDto);

        Field field = findField(taskDto.getField().id(), farmer);

        List<Tool> tools = findTools(taskDto, farmer);

        List<ToolCategory> categories = findToolCategories(taskDto, farmer);

        TaskType prioType = mapStringToTaskType(taskDto.getTaskType());

        Task task = new Task(taskDto.getName(),
            taskDto.getWindowStart(),
            taskDto.getWindowEnd(),
            taskDto.getAssignedStart(),
            taskDto.getAssignedEnd(),
            field,
            tools,
            categories,
            taskDto.getWorkTimeEstimate(),
            taskDto.isFixed(),
            prioType
        );
        task.setOwner(farmer);
        task.setOwnerId(farmer.getId());

        return taskMapper.mapTaskToDto(taskRepository.save(task));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskDto> getAllTasks() {
        LOGGER.info("Getting all tasks");
        var farmer = getFarmer();

        return taskRepository.findAllByOwner(farmer).stream()
            .map(taskMapper::mapTaskToDto)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public TaskDto getTaskById(Long id) throws NotFoundException, AccessDeniedException {
        LOGGER.info("Getting task with id: {}", id);
        var farmer = getFarmer();

        Task task = taskRepository.findById(id).orElseThrow(() -> {
            LOGGER.info("Task with id {} not found", id);
            return new NotFoundException("Task does not exist");
        });

        if (!task.getOwner().getId().equals(farmer.getId())) {
            LOGGER.info("Farmer id {} not allowed to view task owned by {}", farmer.getId(), task.getOwner().getId());
            throw new AccessDeniedException("Not allowed to view this task");
        }

        return taskMapper.mapTaskToDto(task);
    }

    @Override
    @Transactional
    public TaskDto updateTask(Long id, TaskDto taskDto) throws ValidationException, NotFoundException, AccessDeniedException {
        LOGGER.info("Updating task with id {} with new values {}", id, taskDto);
        var farmer = getFarmer();
        taskValidator.validateForUpdate(taskDto);

        Task task = taskRepository.findById(id).orElseThrow(() -> {
            LOGGER.info("Task with id {} not found", id);
            return new NotFoundException("Task to update does not exist");
        });

        if (!task.getOwner().getId().equals(farmer.getId())) {
            LOGGER.info("Farmer id {} not allowed to update task owned by {}", farmer.getId(), task.getOwner().getId());
            throw new AccessDeniedException("Not allowed to update this task");
        }

        Field field = findField(taskDto.getField().id(), farmer);
        List<Tool> tools = findTools(taskDto, farmer);
        List<ToolCategory> categories = findToolCategories(taskDto, farmer);

        TaskType taskType = mapStringToTaskType(taskDto.getTaskType());

        task.setName(taskDto.getName());
        task.setWindowStart(taskDto.getWindowStart());
        task.setWindowEnd(taskDto.getWindowEnd());
        task.setAssignedStart(taskDto.getAssignedStart());
        task.setAssignedEnd(taskDto.getAssignedEnd());
        task.setWorkTimeEstimate(taskDto.getWorkTimeEstimate());
        task.setField(field);
        task.setTools(tools);
        task.setCategories(categories);
        task.setFixed(taskDto.isFixed());
        task.setTaskType(taskType);

        return taskMapper.mapTaskToDto(taskRepository.save(task));
    }

    @Override
    @Transactional
    public void deleteTask(Long id) throws NotFoundException, AccessDeniedException {
        LOGGER.info("Deleting task with id: {}", id);
        var farmer = getFarmer();

        Task task = taskRepository.findById(id).orElseThrow(() -> {
            LOGGER.info("Task with id {} not found", id);
            return new NotFoundException("Task to delete does not exist");
        });

        if (!task.getOwner().getId().equals(farmer.getId())) {
            LOGGER.info("Farmer id {} not allowed to delete task owned by {}", farmer.getId(), task.getOwner().getId());
            throw new AccessDeniedException("Not allowed to delete this task");
        }

        List<PlannedTask> plannedTasks = plannedTaskRepository.findAllByTask(task);
        plannedTaskRepository.deleteAll(plannedTasks);

        taskRepository.delete(task);
    }

    private Farmer getFarmer() throws NotFoundException {
        var activeUser = JwtUtil.getActiveJwtUser();

        return farmerRepository.findById(activeUser.id()).orElseThrow(() -> {
            LOGGER.info("Farmer with id {} not found", activeUser.id());
            return new NotFoundException(String.format("Could not find a farmer with the id %s", activeUser.id()));
        });
    }

    private Field findField(Long id, Farmer farmer) {
        return fieldRepository.findByIdAndOwner(id, farmer)
            .orElseThrow(() -> new NotFoundException("Field with Id " + id + " not found"));
    }

    private List<ToolCategory> findToolCategories(TaskDto taskDto, Farmer farmer) {
        if (taskDto.getCategories() == null || taskDto.getCategories().isEmpty()) {
            return new java.util.ArrayList<>();
        }

        return taskDto.getCategories().stream()
            .map(catDto -> toolCategoryRepository.findByIdAndOwner(catDto.getId(), farmer)
                .orElseThrow(() ->
                    new NotFoundException("Tool category not found: id=" + catDto.getId())))
            .collect(Collectors.toCollection(java.util.ArrayList::new));
    }

    private List<Tool> findTools(TaskDto taskDto, Farmer farmer) {
        if (taskDto.getTools() == null || taskDto.getTools().isEmpty()) {
            return new java.util.ArrayList<>();
        }

        return taskDto.getTools().stream()
            .map(toolDto -> toolRepository.findByIdAndOwner(toolDto.getId(), farmer)
                .orElseThrow(() ->
                    new NotFoundException("Tool not found: id=" + toolDto.getId())))
            .collect(Collectors.toCollection(java.util.ArrayList::new));
    }

    private TaskType mapStringToTaskType(String typeString) {
        if (typeString == null || typeString.isBlank()) {
            return TaskType.NONE;
        }
        try {
            return TaskType.valueOf(typeString.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            LOGGER.warn("Invalid TaskType received: {}: Defaulted to NONE.", typeString);
            //default to NONE if invalid string.
            return TaskType.NONE;
        }
    }
}
