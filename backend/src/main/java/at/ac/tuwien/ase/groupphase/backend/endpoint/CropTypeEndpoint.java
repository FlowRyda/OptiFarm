package at.ac.tuwien.ase.groupphase.backend.endpoint;

import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.CropTypeDto;
import at.ac.tuwien.ase.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.ase.groupphase.backend.service.CropTypeService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
@RequestMapping("/api/v1/croptype")
public class CropTypeEndpoint {
    private static final Logger logger = LoggerFactory.getLogger(CropTypeEndpoint.class);
    private final CropTypeService service;

    public CropTypeEndpoint(CropTypeService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<CropTypeDto>> getAll() {
        logger.info("Request received: GET /api/v1/croptypes");
        List<CropTypeDto> cropTypes = service.findAll();
        logger.info("Returning {} crop types", cropTypes.size());
        return ResponseEntity.ok(cropTypes);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CropTypeDto> getById(@PathVariable("id") Long id) {
        logger.info("Request received: GET /api/v1/croptypes/{}", id);
        CropTypeDto dto = service.findById(id);
        if (dto != null) {
            logger.info("Found crop type with id {}", id);
            return ResponseEntity.ok(dto);
        } else {
            logger.warn("Crop type with id {} not found", id);
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/create")
    public ResponseEntity<CropTypeDto> create(@Valid @RequestBody CropTypeDto dto) throws ValidationException {
        logger.info("Request received: POST /api/v1/croptypes - Payload: {}", dto);
        CropTypeDto saved = service.create(dto);
        logger.info("Created crop type with id {}", saved.getId());
        return ResponseEntity.status(201).body(saved);
    }

    @PutMapping("/edit/{id}")
    public ResponseEntity<CropTypeDto> update(@PathVariable("id") Long id, @Valid @RequestBody CropTypeDto dto) throws ValidationException {
        logger.info("Request received: PUT /api/v1/croptypes/{} - Payload: {}", id, dto);
        CropTypeDto updated = service.update(id, dto);
        logger.info("Updated crop type with id {}", updated.getId());
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        logger.info("Request received: DELETE /api/v1/croptypes/{}", id);
        try {
            service.delete(id);
            logger.info("Deleted crop type with id {}", id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            logger.error("Error deleting crop type with id {}", id, e);
            return ResponseEntity.status(500).build();
        }
    }
}

