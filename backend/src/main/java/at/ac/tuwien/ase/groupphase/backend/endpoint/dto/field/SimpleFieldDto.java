package at.ac.tuwien.ase.groupphase.backend.endpoint.dto.field;

import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.CoordinateDto;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record SimpleFieldDto(
    @NotNull(message = "Field ID must not be null")
    Long id,
    @NotNull(message = "Field name must not be null")
    String name,
    @NotNull(message = "Field boundaries must not be null")
    List<CoordinateDto> boundaries,
    List<List<CoordinateDto>> exclusions
) {
}
