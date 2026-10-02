package at.ac.tuwien.ase.groupphase.backend.endpoint;

import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.schedule.PlannedTaskDto;
import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.schedule.PlannedTaskUpdateDto;
import at.ac.tuwien.ase.groupphase.backend.exception.AccessDeniedException;
import at.ac.tuwien.ase.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.ase.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.ase.groupphase.backend.service.PlannedTaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.lang.invoke.MethodHandles;

@RestController
@RequestMapping(value = "api/v1/planned-tasks")
public class PlannedTaskEndpoint {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final PlannedTaskService plannedTaskService;

    @Autowired
    public PlannedTaskEndpoint(PlannedTaskService plannedTaskService) {
        this.plannedTaskService = plannedTaskService;
    }

    @PutMapping("{id}")
    public ResponseEntity<PlannedTaskDto> update(@PathVariable("id") Long id, @RequestBody PlannedTaskUpdateDto taskDto)
        throws ValidationException, NotFoundException, AccessDeniedException {
        LOGGER.info("Updating PlannedTask with id {}: {}", id, taskDto);
        return ResponseEntity.ok(plannedTaskService.update(id, taskDto));
    }

    @DeleteMapping("{id}")
    public void delete(@PathVariable("id") Long id) throws NotFoundException, AccessDeniedException {
        LOGGER.info("Deleting PlannedTask with id {}", id);
        plannedTaskService.delete(id);
    }
}
