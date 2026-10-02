package at.ac.tuwien.ase.groupphase.backend.service.validation;

import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.ToolDto;
import at.ac.tuwien.ase.groupphase.backend.exception.ValidationException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ToolValidator {
    public void validateForCreate(ToolDto tool) throws ValidationException {
        List<String> validationErrors = commonValidate(tool);

        if (!validationErrors.isEmpty()) {
            throw new ValidationException("Validation Error", validationErrors);
        }
    }

    public void validateForUpdate(ToolDto tool) throws ValidationException {
        List<String> validationErrors = commonValidate(tool);

        if (!validationErrors.isEmpty()) {
            throw new ValidationException("Validation Error", validationErrors);
        }
    }

    public List<String> commonValidate(ToolDto tool) {
        List<String> validationErrors = new ArrayList<>();

        if (tool == null) {
            validationErrors.add("Equipment can not be null");
            return validationErrors;
        }

        if (tool.getName() == null || tool.getName().isBlank()) {
            validationErrors.add("Name is required");
        } else if (tool.getName().length() > 128) {
            validationErrors.add("Name can be at most 128 characters long");
        }

        if (tool.getDescription() == null || tool.getDescription().isBlank()) {
            validationErrors.add("Description is required");
        } else if (tool.getDescription().length() > 4096) {
            validationErrors.add("Description can be at most 4096 characters long");
        }

        if (tool.getRemainingValue() < 0) {
            validationErrors.add("Remaining Value cannot be negative");
        }

        if (tool.getCategoryDto() == null
            || tool.getCategoryDto().getName() == null
            || tool.getCategoryDto().getName().isBlank()) {
            validationErrors.add("Category is required");
        }

        if (tool.getGarageDto() == null) {
            validationErrors.add("Garage is required");
        }

        if (tool.getLocationDto() == null) {
            validationErrors.add("Location is required");
        } else {
            if (tool.getLocationDto().getLon() < -90.0 || tool.getLocationDto().getLon() > 90.0) {
                validationErrors.add("Latitude (y) must be between -90 and 90.");
            }
            if (tool.getLocationDto().getLat() < -180.0 || tool.getLocationDto().getLat() > 180.0) {
                validationErrors.add("Longitude (x) must be between -180 and 180.");
            }
        }

        if (tool.getImage() != null) {
            if (tool.getImage().length() > 2_000_000) {
                validationErrors.add("Image size cannot exceed 2MB");
            }
        }

        return validationErrors;
    }
}
