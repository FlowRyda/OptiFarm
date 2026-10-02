package at.ac.tuwien.ase.groupphase.backend.repository;

import at.ac.tuwien.ase.groupphase.backend.entity.Crop;
import at.ac.tuwien.ase.groupphase.backend.entity.Farmer;
import at.ac.tuwien.ase.groupphase.backend.entity.Field;
import at.ac.tuwien.ase.groupphase.backend.entity.Farmer;
import at.ac.tuwien.ase.groupphase.backend.entity.Vehicle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CropRepository extends JpaRepository<Crop, Long> {
    List<Crop> findByTypeId(Long cropTypeId);

    List<Crop> findByFieldAndOwner(Field field, Farmer owner);

    List<Crop> findByField_Id(Long fieldId);

    @EntityGraph(attributePaths = {"type", "owner"})
    Page<Crop> findAllByOwner(Farmer farmer, Pageable pageable);

    void deleteAllByField(Field field);
}
