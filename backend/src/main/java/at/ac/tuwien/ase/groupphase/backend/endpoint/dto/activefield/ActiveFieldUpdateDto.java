package at.ac.tuwien.ase.groupphase.backend.endpoint.dto.activefield;

import java.time.LocalDateTime;

public record ActiveFieldUpdateDto(
    LocalDateTime endDate,
    LocalDateTime startDate,
    LocalDateTime expectedDate,
    long cropTypeId,
    long fieldId,
    long farmerId
) {
}
