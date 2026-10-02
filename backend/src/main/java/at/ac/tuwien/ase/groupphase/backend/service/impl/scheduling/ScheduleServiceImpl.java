package at.ac.tuwien.ase.groupphase.backend.service.impl.scheduling;

import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.schedule.PlannedTaskDto;
import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.schedule.ScheduleDto;
import at.ac.tuwien.ase.groupphase.backend.endpoint.mapper.ScheduleMapper;
import at.ac.tuwien.ase.groupphase.backend.entity.*;
import at.ac.tuwien.ase.groupphase.backend.exception.AccessDeniedException;
import at.ac.tuwien.ase.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.ase.groupphase.backend.repository.*;
import at.ac.tuwien.ase.groupphase.backend.security.JwtUtil;
import at.ac.tuwien.ase.groupphase.backend.service.ForecastService;
import at.ac.tuwien.ase.groupphase.backend.service.ScheduleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.invoke.MethodHandles;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Primary
@Service
public class ScheduleServiceImpl implements ScheduleService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final ScheduleMapper scheduleMapper;
    private final ScheduleRepository scheduleRepository;
    private final TaskRepository taskRepository;
    private final FarmerRepository farmerRepository;
    private final PlannedTaskRepository plannedTaskRepository;
    private final ToolRepository toolRepository;
    private final ActiveFieldRepository activeFieldRepository;
    private final ForecastService forecastService;

    @Autowired
    public ScheduleServiceImpl(ScheduleRepository scheduleRepository, TaskRepository taskRepository,
                               PlannedTaskRepository plannedTaskRepository, FarmerRepository farmerRepository,
                               ToolRepository toolRepository, ScheduleMapper scheduleMapper, ActiveFieldRepository activeFieldRepository,
                               ForecastService forecastService) {
        this.scheduleRepository = scheduleRepository;
        this.taskRepository = taskRepository;
        this.plannedTaskRepository = plannedTaskRepository;
        this.farmerRepository = farmerRepository;
        this.toolRepository = toolRepository;
        this.scheduleMapper = scheduleMapper;
        this.activeFieldRepository = activeFieldRepository;
        this.forecastService = forecastService;
    }

    @Override
    @Transactional(readOnly = true)
    public ScheduleDto getScheduleById(Long id) {
        Optional<Schedule> schedule = scheduleRepository.findByIdAndOwner(id, getFarmer());
        return schedule.map(scheduleMapper::toDto).orElseThrow(() -> {
            LOGGER.info("Schedule with id {} not found", id);
            return new NotFoundException(String.format("Could not find a schedule with the id %s", id));
        });
    }

    @Override
    @Transactional(readOnly = true)
    public List<ScheduleDto> getAllSchedules() {
        List<Schedule> schedules = scheduleRepository.findAllByOwner(getFarmer());
        return scheduleMapper.toDtoList(schedules);
    }

    @Override
    @Transactional
    public ScheduleDto updateSchedule(Long id, ScheduleDto scheduleDto) {
        LOGGER.info("Updating schedule with id {}", id);

        Schedule schedule = scheduleRepository.findByIdAndOwner(id, getFarmer())
            .orElseThrow(() -> new NotFoundException("No schedule found with id " + id));

        if (scheduleDto.getName() != null) {
            schedule.setName(scheduleDto.getName());
        }

        if (scheduleDto.getPlannedTasks() != null) {
            for (PlannedTaskDto plannedTaskDto : scheduleDto.getPlannedTasks()) {
                PlannedTask plannedTask = schedule.getPlannedTasks().stream()
                    .filter(pt -> pt.getId().equals(plannedTaskDto.getId()))
                    .findFirst()
                    .orElse(null);

                if (plannedTask != null) {
                    if (plannedTaskDto.getAssignedStart() != null) {
                        plannedTask.setAssignedStart(plannedTaskDto.getAssignedStart());
                    }
                    if (plannedTaskDto.getAssignedEnd() != null) {
                        plannedTask.setAssignedEnd(plannedTaskDto.getAssignedEnd());
                    }

                    plannedTask.setFixed(plannedTaskDto.isFixed());

                    Task underlyingTask = plannedTask.getTask();
                    if (underlyingTask != null) {
                        underlyingTask.setFixed(plannedTaskDto.isFixed());

                        if (plannedTaskDto.getAssignedStart() != null && plannedTaskDto.getAssignedEnd() != null) {
                            underlyingTask.setAssignedStart(plannedTaskDto.getAssignedStart());
                            underlyingTask.setAssignedEnd(plannedTaskDto.getAssignedEnd());
                        }

                        taskRepository.save(underlyingTask);
                    }
                }
            }

            plannedTaskRepository.saveAll(schedule.getPlannedTasks());
        }

        schedule = scheduleRepository.save(schedule);
        LOGGER.info("Successfully updated schedule with id {}", id);

        return scheduleMapper.toDto(schedule);
    }

    @Override
    @Transactional
    public ScheduleDto computeNewSchedule() {
        //Step 0: Initialization and data gathering
        Farmer farmer = getFarmer();

        Schedule schedule = new Schedule();
        schedule.setName("Computed Schedule");
        schedule.setOwner(farmer);
        schedule.setOwnerId(farmer.getId());
        schedule = scheduleRepository.save(schedule);

        List<Task> tasks = taskRepository.findAllByOwnerWithCategories(farmer);
        List<Tool> tools = toolRepository.findAllByOwner(farmer);

        //invoke scheduler
        GridScheduler scheduler = new GridScheduler(tasks, tools, schedule, activeFieldRepository, forecastService);
        List<PlannedTask> plannedTasks = scheduler.compute();
        plannedTasks.forEach(pt -> pt.setOwnerId(farmer.getId()));
        Schedule finalSchedule = schedule;
        plannedTasks.forEach(pt -> pt.setSchedule(finalSchedule));

        // persist planned tasks
        plannedTasks = plannedTaskRepository.saveAll(plannedTasks);
        schedule.setPlannedTasks(plannedTasks);

        schedule = scheduleRepository.save(schedule);
        LOGGER.info("Successfully created schedule with id {}", schedule.getId());
        return scheduleMapper.toDto(schedule);
    }

    @Override
    @Transactional
    public ScheduleDto recomputeSchedule(ScheduleDto currentSchedule) {
        Farmer owner = getFarmer();
        Schedule schedule = scheduleRepository.findByIdAndOwner(currentSchedule.getId(), owner)
            .orElseThrow(() -> new NotFoundException("Schedule not found"));

        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);

        List<PlannedTask> existingPlanned = schedule.getPlannedTasks();
        List<PlannedTask> history = existingPlanned.stream()
            .filter(pt -> pt.getAssignedEnd() != null && pt.getAssignedEnd().isBefore(now))
            .toList();

        Set<Long> historyTaskIds = history.stream()
            .map(pt -> pt.getTask().getId())
            .collect(Collectors.toSet());

        final List<Task> allTasks = taskRepository.findAllByOwner(owner);
        final List<Tool> allTools = toolRepository.findAllByOwner(owner);

        final List<PlannedTask> futureToDelete = existingPlanned.stream()
            .filter(pt -> !history.contains(pt))
            .toList();

        final List<Task> tasksToSchedule = allTasks.stream()
            .filter(t -> !historyTaskIds.contains(t.getId()))
            .toList();

        futureToDelete.forEach(pt -> pt.getFixedEquipment().clear());
        schedule.getPlannedTasks().removeAll(futureToDelete);
        plannedTaskRepository.deleteAll(futureToDelete);

        GridScheduler scheduler = new GridScheduler(tasksToSchedule, allTools, schedule, activeFieldRepository,  forecastService);
        scheduler.registerExternalBlockers(history);
        List<PlannedTask> newPlan = scheduler.compute();
        newPlan.forEach(pt -> pt.setOwnerId(owner.getId()));

        List<PlannedTask> merged = new ArrayList<>(history);
        merged.addAll(newPlan);

        if (schedule.getPlannedTasks() == null) {
            schedule.setPlannedTasks(new ArrayList<>());
        }
        schedule.getPlannedTasks().clear();
        schedule.getPlannedTasks().addAll(merged);

        return scheduleMapper.toDto(scheduleRepository.save(schedule));
    }

    @Override
    @Transactional
    public void deleteSchedule(Long id) throws NotFoundException, AccessDeniedException {
        LOGGER.info("Delete schedule with id {}", id);
        var farmer = getFarmer();

        var schedule = scheduleRepository.findById(id).orElseThrow(() ->
            new NotFoundException("Schedule to delete was not found")
        );

        if (!schedule.getOwner().getId().equals(farmer.getId())) {
            throw new AccessDeniedException("You do not have permission to delete this schedule");
        }

        //clears planned_task_tool join table entries
        for (PlannedTask pt : schedule.getPlannedTasks()) {
            pt.getFixedEquipment().clear();
        }

        scheduleRepository.delete(schedule);
    }

    @Override
    public ScheduleDto saveSchedule(ScheduleDto scheduleDto) {
        LOGGER.info("Saving schedule with name: {}", scheduleDto.getName());

        Schedule existingSchedule = scheduleRepository.findByIdAndOwner(scheduleDto.getId(), getFarmer())
            .orElseThrow(() -> new NotFoundException("No schedule found with id " + scheduleDto.getId()));

        if (scheduleDto.getName() != null && !scheduleDto.getName().trim().isEmpty()) {
            existingSchedule.setName(scheduleDto.getName().trim());
        }

        Schedule savedSchedule = scheduleRepository.save(existingSchedule);
        LOGGER.info("Successfully saved schedule with id {} and name {}", savedSchedule.getId(), savedSchedule.getName());

        return scheduleMapper.toDto(savedSchedule);
    }

    private Farmer getFarmer() {
        var activeUser = JwtUtil.getActiveJwtUser();

        return farmerRepository.findById(activeUser.id()).orElseThrow(() -> {
            LOGGER.info("Farmer with id {} not found", activeUser.id());
            return new NotFoundException(String.format("Could not find a farmer with the id %s", activeUser.id()));
        });
    }
}
