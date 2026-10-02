package at.ac.tuwien.ase.groupphase.backend.integrationtest.steps.seed;

import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.SeedDto;

public record EditSeedInput(Long id, SeedDto seedDto) {
}
