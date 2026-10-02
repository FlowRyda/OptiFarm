package at.ac.tuwien.ase.groupphase.backend.service.validation;

import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.VehicleDto;
import at.ac.tuwien.ase.groupphase.backend.exception.ValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class VehicleValidator {
    private final ToolValidator toolValidator;

    @Autowired
    public VehicleValidator(ToolValidator toolValidator) {
        this.toolValidator = toolValidator;
    }

    public void validateForCreate(VehicleDto vehicle) throws ValidationException {
        List<String> validationErrors = commonValidate(vehicle);

        if (!validationErrors.isEmpty()) {
            throw new ValidationException("Validation Error", validationErrors);
        }
    }

    public void validateForUpdate(VehicleDto vehicle) throws ValidationException {
        List<String> validationErrors = commonValidate(vehicle);

        if (!validationErrors.isEmpty()) {
            throw new ValidationException("Validation Error", validationErrors);
        }
    }

    public List<String> commonValidate(VehicleDto vehicle) {
        List<String> validationErrors = new ArrayList<>();

        if (vehicle == null) {
            validationErrors.add("Vehicle can not be null");
            return validationErrors;
        }

        validationErrors.addAll(toolValidator.commonValidate(vehicle));

        if (vehicle.getDrivenKm() < 0) {
            validationErrors.add("Driven kilometers cannot be negative");
        }

        if (vehicle.getFuelLevel() < 0) {
            validationErrors.add("Fuel level cannot be negative");
        }

        return validationErrors;
    }
}
