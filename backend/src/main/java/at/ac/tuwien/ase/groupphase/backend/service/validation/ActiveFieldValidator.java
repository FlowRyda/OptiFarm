package at.ac.tuwien.ase.groupphase.backend.service.validation;

import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.activefield.ActiveFieldCreateDto;
import at.ac.tuwien.ase.groupphase.backend.entity.Farmer;
import at.ac.tuwien.ase.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.ase.groupphase.backend.repository.ActiveFieldRepository;
import at.ac.tuwien.ase.groupphase.backend.repository.FieldRepository;
import at.ac.tuwien.ase.groupphase.backend.repository.SeedRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class ActiveFieldValidator {
    private static final Logger LOGGER = LoggerFactory.getLogger(ActiveFieldValidator.class);

    private final ActiveFieldRepository activeFieldRepository;
    private final SeedRepository seedRepository;
    private final FieldRepository fieldRepository;

    public ActiveFieldValidator(ActiveFieldRepository activeFieldRepository,
                                SeedRepository seedRepository,
                                FieldRepository fieldRepository) {
        this.activeFieldRepository = activeFieldRepository;
        this.seedRepository = seedRepository;
        this.fieldRepository = fieldRepository;
    }

    public void validateCreate(ActiveFieldCreateDto dto, Farmer owner) throws ValidationException {
        List<String> errors = new ArrayList<>();

        if (dto.startDate().isAfter(LocalDateTime.now())) {
            errors.add("Planted date cannot be in the future");
        }

        if (dto.startDate().isBefore(LocalDateTime.now().minusYears(5))) {
            errors.add("Planted date cannot be more than 5 years in the past");
        }

        var seedOpt = seedRepository.findById(dto.cropTypeId());
        if (seedOpt.isEmpty()) {
            errors.add("Seed not found: " + dto.cropTypeId());
        } else if (!seedOpt.get().getOwner().equals(owner)) {
            errors.add("Seed does not belong to you");
        }

        var fieldOpt = fieldRepository.findById(dto.fieldId());
        if (fieldOpt.isEmpty()) {
            errors.add("Field not found: " + dto.fieldId());
        } else if (!fieldOpt.get().getOwner().equals(owner)) {
            errors.add("Field does not belong to you");
        }

        if (dto.sellingPrice() < 0) {
            errors.add("Selling price must be positive or zero");
        }

        if (dto.sellingPrice() > 100000) {
            errors.add("Selling price cannot exceed $100,000");
        }

        if (dto.expectedHarvestDate() != null) {
            if (dto.expectedHarvestDate().isBefore(dto.startDate().plusDays(7))) {
                errors.add("Expected harvest date must be at least 7 day after planting");
            }

            if (dto.expectedHarvestDate().isAfter(dto.startDate().plusYears(5))) {
                errors.add("Expected harvest date cannot be more than 5 years after planting");
            }
        }

        if (activeFieldRepository.findByFieldIdAndEndDateIsNull(dto.fieldId()).isPresent()) {
            errors.add("This field already has an active crop");
        }

        if (!errors.isEmpty()) {
            LOGGER.warn("Active field validation failed: {}", errors);
            throw new ValidationException("Active field validation failed", errors);
        }
    }
}