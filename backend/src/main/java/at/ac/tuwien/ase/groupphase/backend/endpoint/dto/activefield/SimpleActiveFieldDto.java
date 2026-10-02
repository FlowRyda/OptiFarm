package at.ac.tuwien.ase.groupphase.backend.endpoint.dto.activefield;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record SimpleActiveFieldDto(
    @NotNull(message = "Active field ID must not be null")
    Long id,

    @NotNull(message = "Name must not be null")
    String name,

    @PositiveOrZero(message = "Growth status must be 0 or higher")
    int growthStatus,

    @NotNull(message = "Field ID must not be null")
    Long fieldId
) {
}
