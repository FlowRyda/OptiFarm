package at.ac.tuwien.ase.groupphase.backend.endpoint;

import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.CropDetailDto;
import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.CropDto;
import at.ac.tuwien.ase.groupphase.backend.endpoint.mapper.CropMapper;
import at.ac.tuwien.ase.groupphase.backend.entity.Crop;
import at.ac.tuwien.ase.groupphase.backend.repository.CropTypeRepository;
import at.ac.tuwien.ase.groupphase.backend.repository.FarmerRepository;
import at.ac.tuwien.ase.groupphase.backend.service.CropService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/crops")
public class CropEndpoint {
    private static final Logger logger = LoggerFactory.getLogger(CropEndpoint.class);
    private final CropService cropService;
    private final FarmerRepository farmerRepository;
    private final CropTypeRepository cropTypeRepository;

    @Autowired
    public CropEndpoint(CropService cropService,
                        FarmerRepository farmerRepository,
                        CropTypeRepository cropTypeRepository) {
        this.cropService = cropService;
        this.farmerRepository = farmerRepository;
        this.cropTypeRepository = cropTypeRepository;
    }

    @PostMapping
    public ResponseEntity<CropDto> createCrop(@Valid @RequestBody CropDto dto) {
        logger.info("Request received: POST /api/v1/crops - Payload: {}", dto);

        Crop saved = cropService.saveCrop(dto);

        logger.info("Crop created with id {}", saved.getId());
        return ResponseEntity.status(201).body(CropMapper.toDto(saved));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CropDto> getCrop(@PathVariable("id") Long id) {
        logger.info("Request received: GET /api/v1/crops/{}", id);
        Crop crop = cropService.getCropById(id);
        logger.info("Found crop with id {}", id);
        return ResponseEntity.ok(CropMapper.toDto(crop));
    }

    @GetMapping
    public ResponseEntity<List<CropDto>> getAllCrops() {
        logger.info("Request received: GET /api/v1/crops");
        List<CropDto> crops = cropService.getAllCrops().stream()
            .map(CropMapper::toDto)
            .toList();
        logger.info("Returning {} crops", crops.size());
        return ResponseEntity.ok(crops);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCrop(@PathVariable("id") Long id) {
        logger.info("Request received: DELETE /api/v1/crops/{}", id);
        cropService.deleteCrop(id);
        logger.info("Deleted crop with id {}", id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/field/{id}")
    public ResponseEntity<List<CropDetailDto>> getCropsByField(@PathVariable("id") Long id) {
        logger.info("Request received: GET /api/v1/crops/field/{}", id);
        List<CropDetailDto> crops = cropService.getCropsByField(id);
        logger.info("Found {} crops with id {}", crops.size(), id);
        return ResponseEntity.ok(crops);
    }

}
