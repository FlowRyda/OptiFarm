package at.ac.tuwien.ase.groupphase.backend.endpoint;

import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.CropDto;
import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.activefield.ActiveFieldCreateDto;
import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.activefield.ActiveFieldDetailDto;
import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.activefield.ActiveFieldUpdateDto;
import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.activefield.SimpleActiveFieldDto;
import at.ac.tuwien.ase.groupphase.backend.endpoint.mapper.CropMapper;
import at.ac.tuwien.ase.groupphase.backend.entity.Crop;
import at.ac.tuwien.ase.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.ase.groupphase.backend.service.ActiveFieldService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.lang.invoke.MethodHandles;
import java.util.List;


@RestController
@RequestMapping("/api/v1/activeField")
public class ActiveFieldEndpoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final ActiveFieldService activeFieldService;

    public ActiveFieldEndpoint(ActiveFieldService activeFieldService) {
        this.activeFieldService = activeFieldService;
    }


    @GetMapping
    public List<SimpleActiveFieldDto> getAllActiveFields() {
        LOGGER.trace("Getting all active fields");
        return activeFieldService.getAllActiveFields();
    }

    @GetMapping("/current")
    public List<SimpleActiveFieldDto> getAllActiveFieldsWithoutEnddate() {
        LOGGER.trace("Getting all active fields");
        return activeFieldService.getAllActiveFieldsWithoutEndDate();
    }

    @GetMapping("/{id}")
    public ActiveFieldDetailDto getActiveFieldById(@PathVariable(name = "id") Long id) {
        LOGGER.trace("Getting active field for {}", id);
        return activeFieldService.getActiveFieldById(id);
    }

    @GetMapping("/by-field/{id}")
    public ActiveFieldDetailDto getActiveFieldByFieldId(@PathVariable(name = "id") Long id) {
        LOGGER.trace("Getting active field for {}", id);
        return activeFieldService.getActiveFieldByFieldId(id);
    }

    @PutMapping("/edit/{id}")
    public SimpleActiveFieldDto editField(@Valid @RequestBody ActiveFieldUpdateDto activefieldUpdateDto, @PathVariable(name = "id") Long fieldId) {
        LOGGER.trace("Editing active field {}", activefieldUpdateDto.fieldId());
        return activeFieldService.update(activefieldUpdateDto, fieldId);
    }

    @DeleteMapping("/{id}")
    public void deleteFieldById(@PathVariable(name = "id") Long id) {
        LOGGER.trace("Deleting active field with id {}", id);
        activeFieldService.deleteActiveFieldById(id);
    }

    @PostMapping
    public SimpleActiveFieldDto createField(@Valid @RequestBody ActiveFieldCreateDto activeFieldDto) throws ValidationException {
        LOGGER.trace("Creating new active field on field with id {}", activeFieldDto.fieldId());
        return activeFieldService.create(activeFieldDto);
    }

    @PostMapping("/harvest/{id}")
    public ResponseEntity<CropDto> harvestField(@Valid @RequestBody CropDto crop, @PathVariable(name = "id") Long id) {
        LOGGER.trace("Harvesting active field with id {}", id);
        Crop saved = activeFieldService.harvestActiveField(crop, id);
        return ResponseEntity.status(201).body(CropMapper.toDto(saved));
    }

}
