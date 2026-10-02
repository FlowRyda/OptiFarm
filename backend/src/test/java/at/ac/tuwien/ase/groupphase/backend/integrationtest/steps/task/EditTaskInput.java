package at.ac.tuwien.ase.groupphase.backend.integrationtest.steps.task;

import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.schedule.TaskDto;

public record EditTaskInput(Long id, TaskDto taskDto) {
}
