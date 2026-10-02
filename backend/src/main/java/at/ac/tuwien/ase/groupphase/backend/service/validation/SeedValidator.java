package at.ac.tuwien.ase.groupphase.backend.service.validation;

import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.SeedDto;
import at.ac.tuwien.ase.groupphase.backend.entity.Seed;
import at.ac.tuwien.ase.groupphase.backend.exception.ValidationException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class SeedValidator {

    public void validateForCreate(SeedDto dto, List<Seed> existingSeeds) throws ValidationException {
        List<String> validationErrors = commonValidate(dto);
        validationErrors.addAll(validateUniqueNameOnCreate(existingSeeds, dto));

        if (!validationErrors.isEmpty()) {
            throw new ValidationException("Validation Error", validationErrors);
        }
    }

    public void validateForUpdate(SeedDto dto, List<Seed> existingSeeds, Long idToUpdate) throws ValidationException {
        List<String> validationErrors = commonValidate(dto);
        validationErrors.addAll(validateUniqueNameOnUpdate(existingSeeds, dto, idToUpdate));

        if (!validationErrors.isEmpty()) {
            throw new ValidationException("Validation Error", validationErrors);
        }
    }

    public List<String> commonValidate(SeedDto dto) {
        List<String> validationErrors = new ArrayList<>();

        if (dto == null) {
            validationErrors.add("Seed data must not be null");
            return validationErrors;
        }

        if (dto.getName() == null || dto.getName().trim().isEmpty()) {
            validationErrors.add("Seed name must not be empty");
        } else if (dto.getName().length() > 255) {
            validationErrors.add("Seed name can be at most 255 characters long");
        }

        if (dto.getCropTypeId() == null) {
            validationErrors.add("Seed must reference a crop type");
        }

        return validationErrors;
    }

    private List<String> validateUniqueNameOnCreate(List<Seed> existingSeeds, SeedDto dto) {
        String newName = dto.getName() != null ? dto.getName().trim() : "";
        boolean exists = existingSeeds.stream()
            .anyMatch(s -> s.getName().equalsIgnoreCase(newName));
        if (exists) {
            return List.of("A seed with the name '" + dto.getName() + "' already exists.");
        }
        return List.of();
    }

    private List<String> validateUniqueNameOnUpdate(List<Seed> existingSeeds, SeedDto dto, Long idToUpdate) {
        String newName = dto.getName() != null ? dto.getName().trim() : "";
        boolean exists = existingSeeds.stream()
            .filter(s -> !s.getId().equals(idToUpdate))
            .anyMatch(s -> s.getName().equalsIgnoreCase(newName));
        if (exists) {
            return List.of("Another seed with the name '" + dto.getName() + "' already exists.");
        }
        return List.of();
    }
}
