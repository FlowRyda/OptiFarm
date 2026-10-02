package at.ac.tuwien.ase.groupphase.backend.endpoint.mapper;

import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.schedule.PlannedTaskDto;
import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.schedule.ScheduleDto;
import at.ac.tuwien.ase.groupphase.backend.entity.PlannedTask;
import at.ac.tuwien.ase.groupphase.backend.entity.Schedule;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class ScheduleMapper {
    private final PlannedTaskMapper plannedTaskMapper;

    public ScheduleMapper(PlannedTaskMapper plannedTaskMapper) {
        this.plannedTaskMapper = plannedTaskMapper;
    }

    public ScheduleDto toDto(Schedule schedule) {
        if (schedule == null) {
            return null;
        }

        ScheduleDto dto = new ScheduleDto();
        dto.setId(schedule.getId());
        dto.setName(schedule.getName());

        List<PlannedTask> plannedTasks = schedule.getPlannedTasks();
        if (plannedTasks != null && !plannedTasks.isEmpty()) {
            List<PlannedTaskDto> plannedTaskDtos = plannedTasks.stream()
                .filter(Objects::nonNull)
                .map(plannedTaskMapper::toDto)
                .collect(Collectors.toList());
            dto.setPlannedTasks(plannedTaskDtos);
        } else {
            dto.setPlannedTasks(Collections.emptyList());
        }
        return dto;
    }

    public List<ScheduleDto> toDtoList(List<Schedule> schedules) {
        if (schedules == null) {
            return Collections.emptyList();
        }

        return schedules.stream()
            .filter(Objects::nonNull)
            .map(this::toDto)
            .collect(Collectors.toList());
    }

    public Schedule toEntity(ScheduleDto dto) {
        if (dto == null) {
            return null;
        }

        Schedule schedule = new Schedule();
        schedule.setId(dto.getId());
        schedule.setName(dto.getName());

        List<PlannedTaskDto> plannedTaskDtos = dto.getPlannedTasks();
        if (plannedTaskDtos != null && !plannedTaskDtos.isEmpty()) {
            List<PlannedTask> plannedTasks = plannedTaskDtos.stream()
                .filter(Objects::nonNull)
                .map(plannedTaskMapper::toEntity)
                .peek(pt -> pt.setSchedule(schedule))
                .collect(Collectors.toList());
            schedule.setPlannedTasks(plannedTasks);
        } else {
            schedule.setPlannedTasks(Collections.emptyList());
        }

        return schedule;
    }
}
