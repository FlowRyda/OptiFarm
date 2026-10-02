package at.ac.tuwien.ase.groupphase.backend.integrationtest.steps.garage;

import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.garage.GarageDto;

public record EditGarageInput(Long id, GarageDto garageDto) {
}
