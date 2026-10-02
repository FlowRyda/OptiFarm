package at.ac.tuwien.ase.groupphase.backend.repository;


import at.ac.tuwien.ase.groupphase.backend.entity.ActiveField;
import at.ac.tuwien.ase.groupphase.backend.entity.Farmer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ActiveFieldRepository extends JpaRepository<ActiveField, Long> {

    Optional<ActiveField> findByFieldIdAndEndDateIsNull(Long fieldId);

    @EntityGraph(attributePaths = {"field", "seed"})
    List<ActiveField> findAllByOwnerAndEndDateIsNull(Farmer owner);

    @EntityGraph(attributePaths = {"field", "seed"})
    Page<ActiveField> findAllByOwnerAndEndDateIsNull(Farmer farmer, Pageable pageable);

    @EntityGraph(attributePaths = {"field", "seed"})
    List<ActiveField> findAllByOwner(Farmer owner);

    @EntityGraph(attributePaths = {"field", "seed"})
    Page<ActiveField> findAllByOwner(Farmer farmer, Pageable pageable);
}