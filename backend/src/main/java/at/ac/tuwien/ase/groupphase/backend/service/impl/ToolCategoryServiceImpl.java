package at.ac.tuwien.ase.groupphase.backend.service.impl;

import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.ToolCategoryDto;
import at.ac.tuwien.ase.groupphase.backend.entity.Farmer;
import at.ac.tuwien.ase.groupphase.backend.entity.ToolCategory;
import at.ac.tuwien.ase.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.ase.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.ase.groupphase.backend.repository.FarmerRepository;
import at.ac.tuwien.ase.groupphase.backend.repository.ToolCategoryRepository;
import at.ac.tuwien.ase.groupphase.backend.security.JwtUtil;
import at.ac.tuwien.ase.groupphase.backend.service.ToolCategoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class ToolCategoryServiceImpl implements ToolCategoryService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final ToolCategoryRepository toolCategoryRepository;
    private final FarmerRepository farmerRepository;

    @Autowired
    public ToolCategoryServiceImpl(ToolCategoryRepository toolCategoryRepository, FarmerRepository farmerRepository) {
        this.toolCategoryRepository = toolCategoryRepository;
        this.farmerRepository = farmerRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ToolCategoryDto> getAllToolCategories() {
        var farmer = getFarmer();
        List<ToolCategory> categories = toolCategoryRepository.findAllByOwner(farmer);
        return categories.stream()
            .filter(Objects::nonNull)
            .map(this::mapToolCategoryEntityToDto)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ToolCategory getOrCreateToolCategory(String name) throws ValidationException, NotFoundException {
        var farmer = getFarmer();

        LOGGER.info("Getting or creating tool category with name '{}' for farmer '{}'", name, farmer.getId());

        String trimmedName = name == null ? null : name.trim();
        validateCategoryName(trimmedName);

        return toolCategoryRepository.findByNameAndOwner(trimmedName, farmer)
            .orElseGet(() -> toolCategoryRepository.save(new ToolCategory(trimmedName, farmer)));
    }

    private void validateCategoryName(String name) throws ValidationException, NotFoundException {
        List<String> validationErrors = new ArrayList<>();

        if (name == null || name.isBlank()) {
            validationErrors.add("Equipment Category Name cannot be empty");
        } else if (name.length() > 128) {
            validationErrors.add("Equipment Category Name cannot exceed 128 characters");
        }

        if (!validationErrors.isEmpty()) {
            throw new ValidationException("Tool Category Validation Error", validationErrors);
        }
    }

    private Farmer getFarmer() throws NotFoundException {
        var activeUser = JwtUtil.getActiveJwtUser();

        return farmerRepository.findById(activeUser.id()).orElseThrow(() -> {
            return new NotFoundException(String.format("Could not find a farmer with the id %s", activeUser.id()));
        });
    }

    private ToolCategoryDto mapToolCategoryEntityToDto(ToolCategory category) {
        return new ToolCategoryDto(category.getId(), category.getName());
    }
}
