package at.ac.tuwien.ase.groupphase.backend.service;

import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.dashboard.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface DashboardService {

    Page<SeedDashboardDto> getSeeds(Pageable pageable);

    Page<GarageDashboardDto> getGarages(Pageable pageable);

    Page<FieldDashboardDto> getFields(Pageable pageable);

    Page<FieldDashboardDto> getActiveFields(Pageable pageable);

    Page<CropDashboardDto> getCrops(Pageable pageable);

    Page<CropTypeDashboardDto> getCropTypes(Pageable pageable);

    Page<CropTypeDashboardDto> getCropTypesOfOwner(Pageable pageable);

    Page<EquipmentDashboardDto> getEquipment(Pageable pageable);

    Page<ToolCategoryDashboardDto> getToolCategories(Pageable pageable);

    Page<ToolCategoryDashboardDto> getToolCategoriesOfOwner(Pageable pageable);
}