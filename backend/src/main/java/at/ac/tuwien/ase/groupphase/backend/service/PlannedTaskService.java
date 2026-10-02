package at.ac.tuwien.ase.groupphase.backend.service;

import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.schedule.PlannedTaskDto;
import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.schedule.PlannedTaskUpdateDto;
import at.ac.tuwien.ase.groupphase.backend.exception.AccessDeniedException;
import at.ac.tuwien.ase.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.ase.groupphase.backend.exception.ValidationException;

public interface PlannedTaskService {
    /**
     * Updates a PlannedTask and its underlying Task definition.
     *
     * @param id The ID of the PlannedTask
     * @param taskDto The data containing updates for both the Task (name, tools) and the Plan (times)
     * @return The updated PlannedTaskDto
     */
    PlannedTaskDto update(Long id, PlannedTaskUpdateDto taskDto) throws ValidationException, NotFoundException, AccessDeniedException;

    void delete(Long id) throws NotFoundException, AccessDeniedException;
}