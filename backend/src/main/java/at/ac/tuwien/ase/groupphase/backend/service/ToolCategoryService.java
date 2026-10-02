package at.ac.tuwien.ase.groupphase.backend.service;

import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.ToolCategoryDto;
import at.ac.tuwien.ase.groupphase.backend.entity.ToolCategory;
import at.ac.tuwien.ase.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.ase.groupphase.backend.exception.ValidationException;

import java.util.List;

/**
 * Service for managing tool categories owned by a specific farmer.
 */
public interface ToolCategoryService {

    /**
     * Returns all tool categories that belong to logged in farmer.
     *
     * @return list of the farmer's tool category DTOs (never null)
     */
    List<ToolCategoryDto> getAllToolCategories() throws NotFoundException;

    /**
     * Finds an existing category by name for the logged in farmer or creates a new one.
     * Category names are unique per farmer.
     *
     * @param name the category name
     * @return the existing or newly created category
     * @throws ValidationException if the name is invalid
     */
    ToolCategory getOrCreateToolCategory(String name) throws ValidationException, NotFoundException;
}
