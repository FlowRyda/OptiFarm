package at.ac.tuwien.ase.groupphase.backend.repository;

import at.ac.tuwien.ase.groupphase.backend.entity.CropType;
import at.ac.tuwien.ase.groupphase.backend.entity.Farmer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CropTypeRepository extends JpaRepository<CropType, Long> {
    CropType getCropTypeByNameAndOwner(String name, Farmer owner);

    List<CropType> findAllByOwner(Farmer owner);

    Page<CropType> findAllByOwner(Farmer owner, Pageable pageable);

    Optional<CropType> findByIdAndOwnerId(Long id, Long ownerId);

    @Modifying
    void deleteByIdAndOwnerId(Long id, Long ownerId);

    Optional<CropType> findByNameAndOwner(String name, Farmer owner);
}

