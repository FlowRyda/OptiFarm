package at.ac.tuwien.ase.groupphase.backend.integrationtest.steps.cropType;

import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.CropTypeDto;

public record EditCropTypeInput(Long id, CropTypeDto cropTypeDto) {
}
