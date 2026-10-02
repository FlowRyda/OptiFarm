package at.ac.tuwien.ase.groupphase.backend.integrationtest.steps.task;

import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.ToolCategoryDto;
import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.ToolDto;
import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.field.FieldDetailDto;
import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.garage.GarageDto;

public record TaskPrerequisitesDto(
    FieldDetailDto field,
    ToolDto tool,
    ToolCategoryDto category,
    GarageDto garage
) {}
