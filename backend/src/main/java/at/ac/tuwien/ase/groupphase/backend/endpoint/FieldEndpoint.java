package at.ac.tuwien.ase.groupphase.backend.endpoint;

import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.field.FieldCreateDto;
import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.field.FieldDetailDto;
import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.field.FieldUpdateDto;
import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.field.FieldValidationRequestDto;
import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.field.FieldValidationResponseDto;
import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.field.SimpleFieldDto;
import at.ac.tuwien.ase.groupphase.backend.service.FieldService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.invoke.MethodHandles;
import java.util.List;


@RestController
@RequestMapping("/api/v1/field")
public class FieldEndpoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final FieldService fieldService;

    public FieldEndpoint(FieldService fieldService) {
        this.fieldService = fieldService;
    }


    @GetMapping
    public List<SimpleFieldDto> getAllFields() {
        LOGGER.trace("Getting all fields");
        return fieldService.getAllFields();
    }

    @GetMapping("/{id}")
    public FieldDetailDto getFieldById(@PathVariable(name = "id") Long id) {
        LOGGER.trace("Getting field for {}", id);
        return fieldService.getFieldById(id);
    }

    @PutMapping("/edit/{id}")
    public SimpleFieldDto editField(@RequestBody FieldUpdateDto fieldUpdateDto, @PathVariable(name = "id") Long id) {
        LOGGER.trace("Editing field {}", fieldUpdateDto.name());
        return fieldService.update(fieldUpdateDto, id);
    }

    @DeleteMapping("/{id}")
    public void deleteFieldById(@PathVariable(name = "id") Long id) {
        LOGGER.trace("Deleting field with id {}", id);
        fieldService.deleteFieldById(id);
    }

    @PostMapping
    public SimpleFieldDto createField(@RequestBody FieldCreateDto fieldDto) {
        LOGGER.trace("Creating new field {}", fieldDto.name());
        return fieldService.create(fieldDto);
    }

    @PostMapping("/validate")
    public FieldValidationResponseDto validateField(@RequestBody FieldValidationRequestDto request) {
        return fieldService.validateField(request);
    }


}
