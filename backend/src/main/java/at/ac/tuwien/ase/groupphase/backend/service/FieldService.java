package at.ac.tuwien.ase.groupphase.backend.service;

import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.field.FieldCreateDto;
import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.field.FieldDetailDto;
import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.field.FieldUpdateDto;
import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.field.FieldValidationRequestDto;
import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.field.FieldValidationResponseDto;
import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.field.SimpleFieldDto;

import java.util.List;

/**
 * Field service.
 */
public interface FieldService {


    /**
     * Creates a new field with the provided details.
     *
     * @param field the data transfer object containing the field details such as name, boundaries.
     * @return a simplified DTO representing the created field
     */
    SimpleFieldDto create(FieldCreateDto field);

    /**
     * Updates an existing field with the provided details.
     *
     * @param field the data transfer object containing the updated field details.
     * @return a simplified DTO representing the updated field
     */
    SimpleFieldDto update(FieldUpdateDto field, Long id);

    /**
     * Retrieves a field by its unique identifier.
     *
     * @param id the unique identifier for the field to be retrieved.
     * @return a simplified DTO representation of the field.
     */
    FieldDetailDto getFieldById(Long id);

    /**
     * Deletes a field by its unique identifier.
     *
     * @param id the unique identifier for the field to be deleted.
     */
    void deleteFieldById(Long id);

    /**
     * Retrieves all fields.
     *
     * @return List of all fields.
     */
    List<SimpleFieldDto> getAllFields();

    FieldValidationResponseDto validateField(FieldValidationRequestDto request);

}
