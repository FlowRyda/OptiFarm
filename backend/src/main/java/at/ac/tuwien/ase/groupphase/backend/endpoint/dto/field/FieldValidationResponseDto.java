package at.ac.tuwien.ase.groupphase.backend.endpoint.dto.field;


import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.CoordinateDto;

import java.util.List;

public record FieldValidationResponseDto(
    boolean valid,
    String errorMessage,
    List<CoordinateDto> overlappingGeometry,
    String overlappingFieldName
) {
}
