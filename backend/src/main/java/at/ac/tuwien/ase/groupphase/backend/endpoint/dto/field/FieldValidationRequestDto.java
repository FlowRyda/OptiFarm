package at.ac.tuwien.ase.groupphase.backend.endpoint.dto.field;

import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.CoordinateDto;

import java.util.List;

public record FieldValidationRequestDto(
    List<CoordinateDto> boundaries,
    List<List<CoordinateDto>> exclusions,
    Long excludeFieldId
) {
}