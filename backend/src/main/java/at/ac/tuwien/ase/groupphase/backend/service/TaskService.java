package at.ac.tuwien.ase.groupphase.backend.service;

import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.schedule.TaskDto;
import at.ac.tuwien.ase.groupphase.backend.exception.AccessDeniedException;
import at.ac.tuwien.ase.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.ase.groupphase.backend.exception.ValidationException;

import java.util.List;

public interface TaskService {
    TaskDto createTask(TaskDto taskDto) throws ValidationException;

    List<TaskDto> getAllTasks();

    TaskDto getTaskById(Long id) throws NotFoundException, AccessDeniedException;

    TaskDto updateTask(Long id, TaskDto taskDto) throws ValidationException, NotFoundException, AccessDeniedException;

    void deleteTask(Long id) throws NotFoundException, AccessDeniedException;
}
