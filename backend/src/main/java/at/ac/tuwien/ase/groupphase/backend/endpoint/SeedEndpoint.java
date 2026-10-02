package at.ac.tuwien.ase.groupphase.backend.endpoint;

import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.SeedDto;
import at.ac.tuwien.ase.groupphase.backend.endpoint.mapper.SeedMapper;
import at.ac.tuwien.ase.groupphase.backend.entity.CropType;
import at.ac.tuwien.ase.groupphase.backend.entity.Seed;
import at.ac.tuwien.ase.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.ase.groupphase.backend.repository.CropTypeRepository;
import at.ac.tuwien.ase.groupphase.backend.service.SeedService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/seeds")
public class SeedEndpoint {
    private static final Logger logger = LoggerFactory.getLogger(SeedEndpoint.class);
    private final SeedService seedService;
    private final CropTypeRepository cropTypeRepository;

    @Autowired
    public SeedEndpoint(SeedService seedService, CropTypeRepository cropTypeRepository) {
        this.seedService = seedService;
        this.cropTypeRepository = cropTypeRepository;
    }

    @GetMapping
    public ResponseEntity<List<SeedDto>> getAll() {
        logger.info("Request received: GET /api/v1/seeds");
        List<SeedDto> seeds = seedService.getAllSeeds();
        logger.info("Returning {} seeds", seeds.size());
        return ResponseEntity.ok(seeds);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SeedDto> get(@PathVariable("id") Long id) {
        logger.info("Request received: GET /api/v1/seeds/{}", id);
        Seed seed = seedService.getSeedById(id);
        logger.info("Found seed with id {}", id);
        return ResponseEntity.ok(SeedMapper.toDto(seed));
    }

    @PostMapping("/create")
    public ResponseEntity<SeedDto> create(@Valid @RequestBody SeedDto dto) throws ValidationException {
        logger.info("Request received: POST /api/v1/seeds - Payload: {}", dto);

        CropType cropType = cropTypeRepository.findById(dto.getCropTypeId())
            .orElseThrow(() -> {
                logger.error("CropType with id {} not found", dto.getCropTypeId());
                return new RuntimeException("CropType not found");
            });

        Seed saved = seedService.saveSeed(SeedMapper.toEntity(dto, cropType));
        logger.info("Created seed with id {}", saved.getId());
        return ResponseEntity.status(201).body(SeedMapper.toDto(saved));
    }

    @PutMapping("/edit/{id}")
    public ResponseEntity<SeedDto> update(@PathVariable("id") Long id, @Valid @RequestBody SeedDto dto) throws ValidationException {
        logger.info("Request received: PUT /api/v1/seeds/{} - Payload: {}", id, dto);
        SeedDto updated = seedService.update(id, dto);
        logger.info("Updated seed with id {}", updated.getId());
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        logger.info("Request received: DELETE /api/v1/seeds/{}", id);
        seedService.deleteSeed(id);
        logger.info("Deleted seed with id {}", id);
        return ResponseEntity.noContent().build();
    }
}

