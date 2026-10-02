package at.ac.tuwien.ase.groupphase.backend.repository;

import at.ac.tuwien.ase.groupphase.backend.entity.Farmer;
import at.ac.tuwien.ase.groupphase.backend.entity.Seed;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SeedRepository extends JpaRepository<Seed, Long> {
    List<Seed> findByTypeId(Long cropTypeId);

    @EntityGraph(attributePaths = {"type"})
    Page<Seed> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"type"})
    Page<Seed> findAllByOwner(Farmer farmer, Pageable pageable);

    List<Seed> findAllByOwner(Farmer farmer);
}




