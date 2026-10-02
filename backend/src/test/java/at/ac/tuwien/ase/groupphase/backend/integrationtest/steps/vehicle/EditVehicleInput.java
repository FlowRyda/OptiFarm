package at.ac.tuwien.ase.groupphase.backend.integrationtest.steps.vehicle;

import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.VehicleDto;

public record EditVehicleInput(Long id, VehicleDto vehicleDto) {
}
