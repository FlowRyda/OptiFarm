package at.ac.tuwien.ase.groupphase.backend.endpoint.dto.activefield;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.time.LocalDateTime;

public record ActiveFieldDetailDto(
    @NotNull(message = "Active Field ID must not be null")
    Long id,
    @NotNull(message = "Name must not be null")
    String name,
    LocalDateTime harvestDate,
    @NotNull(message = "Expected date must not be null")
    LocalDateTime expectedDate,
    @NotNull(message = "Planted date must not be null")
    LocalDateTime plantedDate,
    @PositiveOrZero(message = "Selling price must be 0 or higher")
    double sellingPrice,
    @PositiveOrZero(message = "Growth status must be 0 or higher")
    int growthStatus,
    @NotNull(message = "Crop type ID must not be null")
    long cropTypeId,
    @NotNull(message = "Field ID must not be null")
    long fieldId,
    @NotNull(message = "Farmer ID must not be null")
    long farmerId,
    @NotNull(message = "Seed name must not be null")
    String seedName,
    @PositiveOrZero(message = "Seed buying price must 0 or higher")
    double seedPrice
) {

}
