package at.ac.tuwien.ase.groupphase.backend.endpoint.mapper;

import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.schedule.TaskDto;
import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.ToolCategoryDto;
import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.ToolDto;
import at.ac.tuwien.ase.groupphase.backend.entity.Task;
import at.ac.tuwien.ase.groupphase.backend.entity.TaskType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TaskMapper {
    private final ToolMapper toolMapper;
    private final FieldMapper fieldMapper;

    @Autowired
    public TaskMapper(ToolMapper toolMapper, FieldMapper fieldMapper) {
        this.fieldMapper = fieldMapper;
        this.toolMapper = toolMapper;
    }

    public TaskDto mapTaskToDto(Task task) {
        if (task == null) {
            return null;
        }

        List<ToolDto> toolDto = task.getTools() == null ? List.of() :
            task.getTools().stream()
                .map(toolMapper::mapToolEntityToToolDto)
                .toList();

        List<ToolCategoryDto> categoryDto = task.getCategories() == null ? List.of() :
            task.getCategories().stream()
                .map(cat -> new ToolCategoryDto(cat.getId(), cat.getName()))
                .toList();

        return new TaskDto(
            task.getId(),
            task.getName(),
            fieldMapper.fieldToFieldDetailDto(task.getField()),
            toolDto,
            categoryDto,
            task.getWindowStart(),
            task.getWindowEnd(),
            task.getAssignedStart(),
            task.getAssignedEnd(),
            task.getWorkTimeEstimate(),
            task.isFixed(),
            task.getTaskType() != null ? task.getTaskType().name() : null
        );
    }
}
