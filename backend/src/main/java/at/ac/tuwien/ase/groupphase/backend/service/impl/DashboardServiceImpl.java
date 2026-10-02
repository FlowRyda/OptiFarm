package at.ac.tuwien.ase.groupphase.backend.service.impl;

import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.dashboard.*;
import at.ac.tuwien.ase.groupphase.backend.endpoint.mapper.*;
import at.ac.tuwien.ase.groupphase.backend.entity.Farmer;
import at.ac.tuwien.ase.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.ase.groupphase.backend.repository.*;
import at.ac.tuwien.ase.groupphase.backend.security.JwtUtil;
import at.ac.tuwien.ase.groupphase.backend.service.DashboardService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;


@Service
public class DashboardServiceImpl implements DashboardService {

    private final SeedRepository seedRepository;
    private final GarageRepository garageRepository;
    private final FieldRepository fieldRepository;
    private final ActiveFieldRepository activeFieldRepository;
    private final CropRepository cropRepository;
    private final CropTypeRepository cropTypeRepository;
    private final VehicleRepository vehicleRepository;
    private final ToolRepository toolRepository;
    private final ToolCategoryRepository toolCategoryRepository;
    private final FarmerRepository farmerRepository;


    public DashboardServiceImpl(SeedRepository seedRepository, GarageRepository garageRepository,
                                FieldRepository fieldRepository, ActiveFieldRepository activeFieldRepository,
                                CropRepository cropRepository, CropTypeRepository cropTypeRepository,
                                VehicleRepository vehicleRepository, ToolRepository toolRepository,
                                ToolCategoryRepository toolCategoryRepository, FarmerRepository farmerRepository) {
        this.seedRepository = seedRepository;
        this.garageRepository = garageRepository;
        this.fieldRepository = fieldRepository;
        this.activeFieldRepository = activeFieldRepository;
        this.cropRepository = cropRepository;
        this.cropTypeRepository = cropTypeRepository;
        this.vehicleRepository = vehicleRepository;
        this.toolRepository = toolRepository;
        this.toolCategoryRepository = toolCategoryRepository;
        this.farmerRepository = farmerRepository;
    }

    @Override
    public Page<SeedDashboardDto> getSeeds(Pageable pageable) {
        var owner = getOwner();
        var seeds = seedRepository.findAllByOwner(owner, pageable);
        return seeds.map(SeedMapper::toDashboardDto);
    }

    @Override
    public Page<GarageDashboardDto> getGarages(Pageable pageable) {
        var owner = getOwner();
        var garages = garageRepository.findAllByOwner(owner, pageable);
        return garages.map(GarageMapper::toDashboardDto);
    }

    @Override
    public Page<FieldDashboardDto> getFields(Pageable pageable) {
        var owner = getOwner();
        var fields = fieldRepository.findAllByOwner(owner, pageable);
        return fields.map(CustomFieldMapper::toDashboardDto);
    }

    @Override
    public Page<FieldDashboardDto> getActiveFields(Pageable pageable) {
        var owner = getOwner();
        var fields = activeFieldRepository.findAllByOwnerAndEndDateIsNull(owner, pageable);
        return fields.map(CustomFieldMapper::toDashboardDto);
    }

    @Override
    public Page<CropDashboardDto> getCrops(Pageable pageable) {
        var owner = getOwner();
        var crops = cropRepository.findAllByOwner(owner, pageable);
        return crops.map(CropMapper::toDashboardDto);
    }

    @Override
    public Page<CropTypeDashboardDto> getCropTypes(Pageable pageable) {
        var cropTypes = cropTypeRepository.findAll(pageable);
        return cropTypes.map(CropTypeMapper::toDashboardDto);
    }

    @Override
    public Page<CropTypeDashboardDto> getCropTypesOfOwner(Pageable pageable) {
        var owner = getOwner();
        var cropTypes = cropTypeRepository.findAllByOwner(owner, pageable);
        return cropTypes.map(CropTypeMapper::toDashboardDto);
    }

    @Override
    public Page<EquipmentDashboardDto> getEquipment(Pageable pageable) {
        var owner = getOwner();
        var tools = toolRepository.findAllByOwner(owner, pageable);
        return tools.map(EquipmentMapper::toDashboardDto);
    }

    @Override
    public Page<ToolCategoryDashboardDto> getToolCategories(Pageable pageable) {
        var toolCategories = toolCategoryRepository.findAll(pageable);
        return toolCategories.map(ToolCategoryMapper::toDashboardDto);
    }

    @Override
    public Page<ToolCategoryDashboardDto> getToolCategoriesOfOwner(Pageable pageable) {
        var owner = getOwner();
        var toolCategories = toolCategoryRepository.findAllByOwner(owner, pageable);
        return toolCategories.map(ToolCategoryMapper::toDashboardDto);
    }

    private Farmer getOwner() {
        var ownerId = JwtUtil.getActiveJwtUser().id();
        return farmerRepository.findById(ownerId).orElseThrow(() -> new NotFoundException("Owner not found"));
    }
}
