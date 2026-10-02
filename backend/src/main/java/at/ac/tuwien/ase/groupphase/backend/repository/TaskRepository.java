package at.ac.tuwien.ase.groupphase.backend.repository;

import at.ac.tuwien.ase.groupphase.backend.entity.Farmer;
import at.ac.tuwien.ase.groupphase.backend.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findAllByOwner(Farmer owner);

    @Query("SELECT t FROM Task t LEFT JOIN FETCH t.categories WHERE t.owner = :owner")
    List<Task> findAllByOwnerWithCategories(@Param("owner") Farmer owner);
}
