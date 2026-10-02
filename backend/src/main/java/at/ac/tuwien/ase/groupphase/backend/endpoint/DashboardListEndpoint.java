package at.ac.tuwien.ase.groupphase.backend.endpoint;

import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.dashboard.*;
import at.ac.tuwien.ase.groupphase.backend.service.DashboardService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardListEndpoint {

    private static final Logger logger = LoggerFactory.getLogger(DashboardListEndpoint.class);

    private final DashboardService service;

    public DashboardListEndpoint(DashboardService service) {
        this.service = service;
    }

    @GetMapping("/seeds")
    public ResponseEntity<Page<SeedDashboardDto>> getAllSeeds(
        @PageableDefault(size = 20)
        Pageable pageable
    ) {
        logger.trace("GET /api/v1/dashboard/seeds pageable={}", pageable);
        Page<SeedDashboardDto> page = service.getSeeds(pageable);
        logger.info("GET /api/v1/dashboard/seeds: pages: {}", page.getTotalElements());
        return ResponseEntity.ok(page);
    }

    @GetMapping("/garages")
    public ResponseEntity<Page<GarageDashboardDto>> getAllGarages(
        @PageableDefault(size = 20)
        Pageable pageable
    ) {
        logger.trace("GET /api/v1/dashboard/garages  pageable={}", pageable);
        Page<GarageDashboardDto> page = service.getGarages(pageable);
        logger.info("GET /api/v1/dashboard/garages: pages: {}", page.getTotalElements());
        return ResponseEntity.ok(page);
    }

    @GetMapping("/fields")
    public ResponseEntity<Page<FieldDashboardDto>> getAllFields(
        @PageableDefault(size = 20)
        Pageable pageable
    ) {
        logger.trace("GET /api/v1/dashboard/fields pageable={}", pageable);
        Page<FieldDashboardDto> page = service.getFields(pageable);
        logger.info("GET /api/v1/dashboard/fields: pages: {}", page.getTotalElements());
        return ResponseEntity.ok(page);
    }

    @GetMapping("/activeFields")
    public ResponseEntity<Page<FieldDashboardDto>> getAllActiveFields(
        @PageableDefault(size = 20)
        Pageable pageable
    ) {
        logger.trace("GET /api/v1/dashboard/activeFields pageable={}", pageable);
        Page<FieldDashboardDto> page = service.getActiveFields(pageable);
        logger.info("GET /api/v1/dashboard/activeFields: pages: {}", page.getTotalElements());
        return ResponseEntity.ok(page);
    }

    @GetMapping("/crops")
    public ResponseEntity<Page<CropDashboardDto>> getAllCrops(
        @PageableDefault(size = 20, sort = "harvestDate", direction = Sort.Direction.DESC)
        Pageable pageable
    ) {
        logger.trace("GET /api/v1/dashboard/crops pageable={}", pageable);
        Page<CropDashboardDto> page = service.getCrops(pageable);
        logger.info("GET /api/v1/dashboard/crops: pages: {}", page.getTotalElements());
        return ResponseEntity.ok(page);
    }

    @GetMapping("/cropTypesOfOwner")
    public ResponseEntity<Page<CropTypeDashboardDto>> getAllCropTypesOfOwner(
        @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC)
        Pageable pageable
    ) {
        logger.trace("GET /api/v1/dashboard/cropTypesOfOwner pageable={}", pageable);
        Page<CropTypeDashboardDto> page = service.getCropTypesOfOwner(pageable);
        logger.info("GET /api/v1/dashboard/cropTypesOfOwner: pages: {}", page.getTotalElements());
        return ResponseEntity.ok(page);
    }

    @GetMapping("/equipment")
    public ResponseEntity<Page<EquipmentDashboardDto>> getAllEquipment(
        @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC)
        Pageable pageable
    ) {
        logger.trace("GET /api/v1/dashboard/equipment pageable={}", pageable);
        Page<EquipmentDashboardDto> page = service.getEquipment(pageable);
        logger.info("GET /api/v1/dashboard/equipment: pages: {}", page.getTotalElements());
        return ResponseEntity.ok(page);
    }

    @GetMapping("/toolCategoriesOfOwner")
    public ResponseEntity<Page<ToolCategoryDashboardDto>> getAllToolCategoriesOfOwner(
        @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC)
        Pageable pageable
    ) {
        logger.trace("GET /api/v1/dashboard/toolCategoriesOfOwner pageable={}", pageable);
        Page<ToolCategoryDashboardDto> page = service.getToolCategoriesOfOwner(pageable);
        logger.info("GET /api/v1/dashboard/toolCategoriesOfOwner: pages: {}", page.getTotalElements());
        return ResponseEntity.ok(page);
    }
}
