package at.ac.tuwien.ase.groupphase.backend.endpoint;

import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.schedule.ScheduleDto;
import at.ac.tuwien.ase.groupphase.backend.service.ScheduleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/schedules")
public class ScheduleEndpoint {
    private static final Logger logger = LoggerFactory.getLogger(ScheduleEndpoint.class);
    private final ScheduleService scheduleService;

    @Autowired
    public ScheduleEndpoint(ScheduleService scheduler) {
        this.scheduleService = scheduler;
    }

    @PostMapping
    public ScheduleDto createSchedule() {
        logger.debug("creating new schedule");
        return scheduleService.computeNewSchedule();
    }

    @PostMapping("/{id}/compute")
    public ScheduleDto recomputeSchedule(@PathVariable("id") Long id) {
        logger.debug("Recomputing schedule with id {}", id);
        ScheduleDto existingSchedule = scheduleService.getScheduleById(id);
        return scheduleService.recomputeSchedule(existingSchedule);
    }

    @PutMapping("/{id}")
    public ScheduleDto updateSchedule(@PathVariable("id") Long id, @RequestBody ScheduleDto scheduleDto) {
        logger.info("Updating schedule with id {}", id);
        return scheduleService.updateSchedule(id, scheduleDto);
    }

    @GetMapping("/{id}")
    public ScheduleDto getById(@PathVariable("id") Long id) {
        logger.debug("Retrieving schedule with id {}", id);
        return scheduleService.getScheduleById(id);
    }

    @GetMapping
    public List<ScheduleDto> getAll() {
        logger.debug("Getting all schedules");
        return scheduleService.getAllSchedules();
    }

    @DeleteMapping("/{id}")
    public void deleteSchedule(@PathVariable("id") Long id) {
        logger.debug("Deleting schedule with id {}", id);
        scheduleService.deleteSchedule(id);
    }

    @PostMapping("/save")
    public ScheduleDto saveSchedule(@RequestBody ScheduleDto scheduleDto) {
        logger.info("Saving schedule with id {} and name {}", scheduleDto.getId(), scheduleDto.getName());
        return scheduleService.saveSchedule(scheduleDto);
    }
}
