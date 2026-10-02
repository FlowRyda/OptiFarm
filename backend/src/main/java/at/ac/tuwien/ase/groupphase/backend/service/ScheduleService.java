package at.ac.tuwien.ase.groupphase.backend.service;

import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.schedule.ScheduleDto;
import at.ac.tuwien.ase.groupphase.backend.exception.AccessDeniedException;
import at.ac.tuwien.ase.groupphase.backend.exception.NotFoundException;

import java.util.List;

public interface ScheduleService {
    /**
     * Retrieve a schedule by its ID.
     *
     * @param id the Id of the Schedule to retrieve
     * @return the Schedule belonging to the given Id
     */
    ScheduleDto getScheduleById(Long id);

    /**
     * Retrieve all schedules.
     *
     * @return a list of all Schedules
     */
    List<ScheduleDto> getAllSchedules();

    /**
     * Updates the scheduled tasks.
     *
     * @param id          the Id of the schedule
     * @param scheduleDto the Dto to be changed
     * @return returns the updated schedule, also updates the planned time of underlying tasks
     */

    ScheduleDto updateSchedule(Long id, ScheduleDto scheduleDto);


    /**
     * Initiates the computation of a new schedule. Resulting schedule shall be conflict free.
     *
     * @return new, conflict-free Schedule
     */
    ScheduleDto computeNewSchedule();

    /**
     * Recomputes an existing schedule based on the given schedule.
     * New schedule shall be conflict-free.
     *
     * @param currentSchedule the current Schedule to be recomputed
     * @return updated, conflict-free Schedule
     */
    ScheduleDto recomputeSchedule(ScheduleDto currentSchedule);

    /**
     * Deletes a schedule by its ID.
     *
     * @param id the Id of the Schedule to delete
     */
    void deleteSchedule(Long id) throws NotFoundException, AccessDeniedException;

    /**
     * Saves a schedule.
     *
     * @param scheduleDto the schedule to be saved
     * @return saves schedule
     */

    ScheduleDto saveSchedule(ScheduleDto scheduleDto);
}
