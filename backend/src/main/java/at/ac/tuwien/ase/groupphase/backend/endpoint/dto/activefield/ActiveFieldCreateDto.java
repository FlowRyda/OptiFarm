package at.ac.tuwien.ase.groupphase.backend.endpoint.dto.activefield;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.time.LocalDateTime;

public record ActiveFieldCreateDto(
    @NotNull(message = "Planted Date must not be null")
    LocalDateTime startDate,
    @NotNull(message = "Crop type ID must not be null")
    long cropTypeId,
    @NotNull(message = "Field ID must not be null")
    long fieldId,
    @PositiveOrZero(message = "Selling price must be positive or zero")
    double sellingPrice,
    @NotNull(message = "Expected Harvest Date must not be null")
    LocalDateTime expectedHarvestDate
) {
}


