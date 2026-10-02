package at.ac.tuwien.ase.groupphase.backend.repository;

import at.ac.tuwien.ase.groupphase.backend.entity.Farmer;
import at.ac.tuwien.ase.groupphase.backend.entity.PlannedTask;
import at.ac.tuwien.ase.groupphase.backend.entity.Schedule;
import at.ac.tuwien.ase.groupphase.backend.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PlannedTaskRepository extends JpaRepository<PlannedTask, Long> {
    List<PlannedTask> findAllByOwner(Farmer owner);

    List<PlannedTask> findAllByTask(Task task);

    Optional<PlannedTask> findByIdAndOwner(Long id, Farmer owner);

    List<PlannedTask> findAllByScheduleAndOwner(Schedule schedule, Farmer owner);


}
