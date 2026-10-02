package at.ac.tuwien.ase.groupphase.backend.endpoint.mapper;

import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.schedule.PlannedTaskDto;
import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.ToolDto;
import at.ac.tuwien.ase.groupphase.backend.entity.PlannedTask;
import at.ac.tuwien.ase.groupphase.backend.entity.Task;
import at.ac.tuwien.ase.groupphase.backend.entity.Tool;
import at.ac.tuwien.ase.groupphase.backend.repository.TaskRepository;
import at.ac.tuwien.ase.groupphase.backend.repository.ToolRepository;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class PlannedTaskMapper {
    private final TaskRepository taskRepository;
    private final ToolMapper toolMapper;
    private final FieldMapper fieldMapper;
    private final ToolRepository toolRepository;

    public PlannedTaskMapper(TaskRepository taskRepository, ToolMapper toolMapper, FieldMapper fieldMapper, ToolRepository toolRepository) {
        this.taskRepository = taskRepository;
        this.toolMapper = toolMapper;
        this.fieldMapper = fieldMapper;
        this.toolRepository = toolRepository;
    }


    public PlannedTaskDto toDto(PlannedTask entity) {
        if (entity == null) {
            return null;
        }
        Task task = entity.getTask();

        PlannedTaskDto dto = new PlannedTaskDto();
        dto.setId(entity.getId());
        dto.setName(task.getName());
        dto.setAssignedStart(entity.getAssignedStart());
        dto.setAssignedEnd(entity.getAssignedEnd());
        dto.setWindowStart(task.getWindowStart());
        dto.setWindowEnd(task.getWindowEnd());
        dto.setFixed(entity.isFixed());
        dto.setTaskId(task.getId());
        dto.setWorkTimeEstimate(task.getWorkTimeEstimate());
        dto.setField(fieldMapper.fieldToFieldDetailDto(task.getField()));
        dto.setCategories(toolMapper.mapToolCategoryEntitiesToToolCategoryDtoList(task.getCategories()));

        if (entity.getFixedEquipment() != null) {
            List<ToolDto> tools = entity.getFixedEquipment().stream()
                .filter(Objects::nonNull)
                .map(toolMapper::mapToolEntityToToolDto)
                .toList();
            dto.setFixedEquipment(tools);
        } else {
            dto.setFixedEquipment(List.of());
        }

        dto.setSchedule(null); // To avoid circular references
        dto.setConflicted(entity.isConflicted());

        return dto;

    }

    // Caller must set back-reference to schedule
    public PlannedTask toEntity(PlannedTaskDto dto) {
        if (dto == null) {
            return null;
        }

        PlannedTask entity = new PlannedTask();
        entity.setId(dto.getId());
        entity.setAssignedStart(dto.getAssignedStart());
        entity.setAssignedEnd(dto.getAssignedEnd());
        entity.setFixed(dto.isFixed());

        // Lookup the Task using taskId
        if (dto.getTaskId() != null) {
            Task task = taskRepository.findById(dto.getTaskId())
                .orElseThrow(() -> new IllegalStateException(
                    "Task not found for id=" + dto.getTaskId()));
            entity.setTask(task);
        } else {
            throw new IllegalArgumentException("PlannedTaskDto must contain taskId");
        }

        // Tools
        if (dto.getFixedEquipment() != null) {
            List<Tool> tools = dto.getFixedEquipment().stream()
                .filter(Objects::nonNull)
                .map(t -> toolRepository.findById(t.getId())
                    .orElseThrow(() -> new IllegalStateException(
                        "Tool not found for id=" + t.getId())))
                .collect(Collectors.toList());
            entity.setFixedEquipment(tools);
        } else {
            entity.setFixedEquipment(Collections.emptyList());
        }

        dto.setConflicted(entity.isConflicted());
        entity.setSchedule(null);   // To avoid circular references
        // back-reference to schedule must be set by caller

        return entity;
    }

    public List<PlannedTaskDto> toDtoList(List<PlannedTask> list) {
        if (list == null) {
            return Collections.emptyList();
        }

        return list.stream()
            .filter(Objects::nonNull)
            .map(this::toDto)
            .collect(Collectors.toList());
    }
}
