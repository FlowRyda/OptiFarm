package at.ac.tuwien.ase.groupphase.backend.endpoint.dto.field;

import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.CoordinateDto;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record FieldUpdateDto(
    @NotNull(message = "Field name must not be null")
    String name,
    @NotNull(message = "Field boundaries must not be null")
    List<CoordinateDto> boundaries,
    List<List<CoordinateDto>> exclusions
) {
}
