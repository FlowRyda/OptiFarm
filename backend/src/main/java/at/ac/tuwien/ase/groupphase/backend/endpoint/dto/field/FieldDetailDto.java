package at.ac.tuwien.ase.groupphase.backend.endpoint.dto.field;

import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.CoordinateDto;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.util.List;

public record FieldDetailDto(
    @NotNull(message = "Field ID must not be null")
    Long id,
    @NotNull(message = "Field name must not be null")
    String name,
    @PositiveOrZero(message = "Area must be bigger or equals 0")
    double area,
    @NotNull(message = "Field boundaries must not be null")
    List<CoordinateDto> boundaries,
    List<List<CoordinateDto>> exclusions
) {

}
