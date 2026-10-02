package at.ac.tuwien.ase.groupphase.backend.repository;

import at.ac.tuwien.ase.groupphase.backend.entity.Farmer;
import at.ac.tuwien.ase.groupphase.backend.entity.Tool;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import at.ac.tuwien.ase.groupphase.backend.entity.ToolCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for accessing and managing {@link Tool} entities.
 */
@Repository
public interface ToolRepository extends JpaRepository<Tool, Long> {

    /**
     * Returns all tools owned by the specified farmer.
     *
     * @param farmer the owner of the tools
     * @return list of tools belonging to the given farmer (never null)
     */
    List<Tool> findAllByOwner(Farmer farmer);

    Page<Tool> findAllByOwner(Farmer farmer, Pageable pageable);

    Optional<Tool> findByNameAndOwner(String name, Farmer owner);

    Optional<Tool> findByIdAndOwner(Long id, Farmer owner);

    Optional<Tool> findById(Long id);

    List<Tool> findByOwnerAndCategory(Farmer owner, ToolCategory category);
}