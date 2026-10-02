package at.ac.tuwien.ase.groupphase.backend.repository;

import at.ac.tuwien.ase.groupphase.backend.entity.Farmer;
import at.ac.tuwien.ase.groupphase.backend.entity.Vehicle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for accessing and managing {@link Vehicle} entities.
 * Provides CRUD operations and custom queries for vehicles based on their owner.
 */
@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

    /**
     * Finds all vehicles owned by the specified farmer.
     *
     * @param farmer the owner of the vehicles
     * @return a list of vehicles belonging to the given farmer
     */
    List<Vehicle> findAllByOwner(Farmer farmer);

    Page<Vehicle> findAllByOwner(Farmer farmer, Pageable pageable);

    Optional<Vehicle> findByNameAndOwner(String name, Farmer owner);
}
