package at.ac.tuwien.ase.groupphase.backend.endpoint.mapper;

import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.dashboard.ToolCategoryDashboardDto;
import at.ac.tuwien.ase.groupphase.backend.entity.ToolCategory;

public class ToolCategoryMapper {
    public static ToolCategoryDashboardDto toDashboardDto(ToolCategory category) {
        if (category == null) {
            return null;
        }

        return new ToolCategoryDashboardDto(category);
    }
}
