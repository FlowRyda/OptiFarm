package at.ac.tuwien.ase.groupphase.backend.repository;

import at.ac.tuwien.ase.groupphase.backend.entity.Farmer;
import at.ac.tuwien.ase.groupphase.backend.entity.ToolCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for accessing and managing {@link ToolCategory} entities.
 */
@Repository
public interface ToolCategoryRepository extends JpaRepository<ToolCategory, Long> {

    /**
     * Finds a tool category with the given name that belongs to the specified farmer.
     *
     * @param name  the category name
     * @param owner the farmer who owns the category
     * @return an {@link Optional} containing the category if found
     */
    Optional<ToolCategory> findByNameAndOwner(String name, Farmer owner);

    /**
     * Returns all tool categories owned by the specified farmer.
     *
     * @param farmer the owner of the categories
     * @return list of categories belonging to that farmer (never null)
     */
    List<ToolCategory> findAllByOwner(Farmer farmer);

    Page<ToolCategory> findAllByOwner(Farmer owner, Pageable pageable);

    Optional<ToolCategory> findByIdAndOwner(Long id, Farmer owner);
}

