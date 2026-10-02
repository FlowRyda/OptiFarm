package at.ac.tuwien.ase.groupphase.backend.service.validation;

import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.CropTypeDto;
import at.ac.tuwien.ase.groupphase.backend.entity.CropType;
import at.ac.tuwien.ase.groupphase.backend.exception.ValidationException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class CropTypeValidator {

    public void validateForCreate(CropTypeDto dto, List<CropType> existingCropTypes) throws ValidationException {
        List<String> validationErrors = commonValidate(dto);
        validationErrors.addAll(validateUniqueNameOnCreate(existingCropTypes, dto));

        if (!validationErrors.isEmpty()) {
            throw new ValidationException("Validation Error", validationErrors);
        }
    }

    public void validateForUpdate(CropTypeDto dto, List<CropType> existingCropTypes, Long idToUpdate) throws ValidationException {
        List<String> validationErrors = commonValidate(dto);
        validationErrors.addAll(validateUniqueNameOnUpdate(existingCropTypes, dto, idToUpdate));

        if (!validationErrors.isEmpty()) {
            throw new ValidationException("Validation Error", validationErrors);
        }
    }

    public List<String> commonValidate(CropTypeDto dto) {
        List<String> validationErrors = new ArrayList<>();

        if (dto == null) {
            validationErrors.add("CropType data must not be null");
            return validationErrors;
        }

        if (dto.getName() == null || dto.getName().trim().isEmpty()) {
            validationErrors.add("CropType name must not be empty");
        } else if (dto.getName().length() > 255) {
            validationErrors.add("CropType name can be at most 255 characters long");
        }

        return validationErrors;
    }

    private List<String> validateUniqueNameOnCreate(List<CropType> existing, CropTypeDto dto) {
        String newName = dto.getName() != null ? dto.getName().trim() : "";
        boolean exists = existing.stream()
            .anyMatch(c -> c.getName().equalsIgnoreCase(newName));
        if (exists) {
            return List.of("A CropType with the name '" + dto.getName() + "' already exists.");
        }
        return List.of();
    }

    private List<String> validateUniqueNameOnUpdate(List<CropType> existing, CropTypeDto dto, Long idToUpdate) {
        String newName = dto.getName() != null ? dto.getName().trim() : "";
        boolean exists = existing.stream()
            .filter(c -> !c.getId().equals(idToUpdate))
            .anyMatch(c -> c.getName().equalsIgnoreCase(newName));
        if (exists) {
            return List.of("Another CropType with the name '" + dto.getName() + "' already exists.");
        }
        return List.of();
    }
}
