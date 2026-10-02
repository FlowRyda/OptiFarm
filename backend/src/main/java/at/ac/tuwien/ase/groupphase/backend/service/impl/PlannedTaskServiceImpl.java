package at.ac.tuwien.ase.groupphase.backend.service.impl;

import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.schedule.PlannedTaskDto;
import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.schedule.PlannedTaskUpdateDto;
import at.ac.tuwien.ase.groupphase.backend.endpoint.mapper.PlannedTaskMapper;
import at.ac.tuwien.ase.groupphase.backend.entity.*;
import at.ac.tuwien.ase.groupphase.backend.exception.AccessDeniedException;
import at.ac.tuwien.ase.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.ase.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.ase.groupphase.backend.repository.*;
import at.ac.tuwien.ase.groupphase.backend.security.JwtUtil;
import at.ac.tuwien.ase.groupphase.backend.service.PlannedTaskService;
import at.ac.tuwien.ase.groupphase.backend.service.TaskService;
import at.ac.tuwien.ase.groupphase.backend.service.validation.PlannedTaskValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class PlannedTaskServiceImpl implements PlannedTaskService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final PlannedTaskRepository plannedTaskRepository;
    private final TaskRepository taskRepository;
    private final FarmerRepository farmerRepository;
    private final FieldRepository fieldRepository;
    private final ToolRepository toolRepository;
    private final ToolCategoryRepository toolCategoryRepository;
    private final PlannedTaskMapper plannedTaskMapper;
    private final TaskService taskService;
    private final PlannedTaskValidator plannedTaskValidator;

    @Autowired
    public PlannedTaskServiceImpl(PlannedTaskRepository plannedTaskRepository,
                                  TaskRepository taskRepository,
                                  FarmerRepository farmerRepository,
                                  FieldRepository fieldRepository,
                                  ToolRepository toolRepository,
                                  ToolCategoryRepository toolCategoryRepository,
                                  PlannedTaskMapper plannedTaskMapper,
                                  TaskService taskService,
                                  PlannedTaskValidator plannedTaskValidator) {
        this.plannedTaskRepository = plannedTaskRepository;
        this.taskRepository = taskRepository;
        this.farmerRepository = farmerRepository;
        this.fieldRepository = fieldRepository;
        this.toolRepository = toolRepository;
        this.toolCategoryRepository = toolCategoryRepository;
        this.plannedTaskMapper = plannedTaskMapper;
        this.taskService = taskService;
        this.plannedTaskValidator = plannedTaskValidator;
    }

    @Override
    @Transactional
    public PlannedTaskDto update(Long id, PlannedTaskUpdateDto updateDto) throws ValidationException, NotFoundException, AccessDeniedException {
        LOGGER.info("Updating PlannedTask {} with data {}", id, updateDto);
        var farmer = getFarmer();

        plannedTaskValidator.validateUpdateObject(updateDto);

        PlannedTask plannedTask = plannedTaskRepository.findByIdAndOwner(id, farmer).orElseThrow(() -> {
            LOGGER.info("PlannedTask with id {} not found", id);
            return new NotFoundException("PlannedTask not found");
        });


        // Security: Access via the underlying Task's owner
        Task innerTask = plannedTask.getTask();
        if (!innerTask.getOwner().getId().equals(farmer.getId())) {
            LOGGER.error("Farmer {} tried to update PlannedTask {} owned by {}", farmer.getId(), id, innerTask.getOwner().getId());
            throw new AccessDeniedException("Not allowed to update this planned task");
        }


        // Update underlying task
        updateTaskDetails(innerTask, updateDto, farmer);

        // Update plannedTask details
        updateSchedulingDetails(plannedTask, updateDto);

        // Save
        PlannedTask savedPlannedTask = plannedTaskRepository.save(plannedTask);

        // Return updated DTO
        return plannedTaskMapper.toDto(savedPlannedTask);
    }

    private void updateTaskDetails(Task task, PlannedTaskUpdateDto dto, Farmer farmer) {
        if (dto.getName() != null) {
            task.setName(dto.getName());
        }

        if (dto.getWorkTimeEstimate() > 0) {
            task.setWorkTimeEstimate(dto.getWorkTimeEstimate());
        }

        if (dto.getWindowStart() != null) {
            task.setWindowStart(dto.getWindowStart());
        }

        if (dto.getWindowEnd() != null) {
            task.setWindowEnd(dto.getWindowEnd());
        }

        // resolve relations
        if (dto.getFieldId() != null) {
            Field field = fieldRepository.findByIdAndOwner(dto.getFieldId(), farmer)
                .orElseThrow(() -> new NotFoundException("Field with id " + dto.getFieldId() + " not found"));
            task.setField(field);
        }

        if (dto.getToolIds() != null) {
            List<Tool> tools = new ArrayList<>();
            for (Long toolId : dto.getToolIds()) {
                Tool tool = toolRepository.findByIdAndOwner(toolId, farmer)
                    .orElseThrow(() -> new NotFoundException("Tool with id " + toolId + " not found"));
                tools.add(tool);
            }
            task.setTools(tools);
        }

        if (dto.getCategoryIds() != null) {
            List<ToolCategory> categories = new ArrayList<>();
            for (Long categoryId : dto.getCategoryIds()) {
                ToolCategory category = toolCategoryRepository.findByIdAndOwner(categoryId, farmer)
                    .orElseThrow(() -> new NotFoundException("Tool category with id " + categoryId + " not found"));
                categories.add(category);
            }
            task.setCategories(categories);
        }

        taskRepository.save(task);
    }

    private void updateSchedulingDetails(PlannedTask plannedTask, PlannedTaskUpdateDto dto) {
        if (dto.getAssignedStart() != null && !Objects.equals(plannedTask.getAssignedStart(), dto.getAssignedStart())) {
            plannedTask.setFixed(true);
        }

        if (dto.isFixed()) {
            plannedTask.setFixed(true);
        }

        plannedTask.setAssignedStart(dto.getAssignedStart());
        plannedTask.setAssignedEnd(dto.getAssignedEnd());
    }

    @Override
    @Transactional
    public void delete(Long id) throws NotFoundException, AccessDeniedException {
        LOGGER.info("Deleting Task belonging to PlannedTask id: {}", id);
        var farmer = getFarmer();

        PlannedTask plannedTask = plannedTaskRepository.findById(id).orElseThrow(() ->
            new NotFoundException("PlannedTask not found"));

        if (!plannedTask.getTask().getOwner().getId().equals(farmer.getId())) {
            throw new AccessDeniedException("Not allowed to modify this planned task");
        }

        Task task = plannedTask.getTask();
        if (!task.getOwner().getId().equals(farmer.getId())) {
            throw new AccessDeniedException("Not allowed to modify this planned task");
        }
        LOGGER.info("Deleting Task id: {}", task.getId());
        taskService.deleteTask(task.getId());
    }

    private Farmer getFarmer() throws NotFoundException {
        var activeUser = JwtUtil.getActiveJwtUser();
        return farmerRepository.findById(activeUser.id()).orElseThrow(() ->
            new NotFoundException(String.format("Could not find a farmer with the id %s", activeUser.id())));
    }
}