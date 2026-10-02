package at.ac.tuwien.ase.groupphase.backend.repository;


import at.ac.tuwien.ase.groupphase.backend.entity.Farmer;
import at.ac.tuwien.ase.groupphase.backend.entity.Field;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FieldRepository extends JpaRepository<Field, Long> {
    List<Field> findAllByOwner(Farmer owner);
    
    Page<Field> findAllByOwner(Farmer farmer, Pageable pageable);

    List<Field> findAllByOwnerAndIdNotIn(Farmer owner, List<Long> excludeIds);

    Field findByNameAndOwner(String name, Farmer owner);

    Optional<Field> findByIdAndOwner(Long id, Farmer owner);
}
