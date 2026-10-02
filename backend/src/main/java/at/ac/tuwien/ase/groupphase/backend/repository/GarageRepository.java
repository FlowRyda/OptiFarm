package at.ac.tuwien.ase.groupphase.backend.repository;

import at.ac.tuwien.ase.groupphase.backend.entity.Farmer;
import at.ac.tuwien.ase.groupphase.backend.entity.Garage;
import at.ac.tuwien.ase.groupphase.backend.entity.Seed;
import at.ac.tuwien.ase.groupphase.backend.entity.Vehicle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GarageRepository extends JpaRepository<Garage, Long> {

    @EntityGraph(attributePaths = {"tools", "tools.category"})
    Optional<Garage> getGarageByIdAndOwnerId(Long id, Long ownerId);

    List<Garage> getAllGaragesByOwnerId(Long id);

    void deleteById(Long id);

    Optional<Garage> findByName(String name);

    @EntityGraph(attributePaths = {"tools"})
    Page<Garage> findAllByOwner(Farmer farmer, Pageable pageable);

    List<Garage> findAllByOwner(Farmer farmer);
}
