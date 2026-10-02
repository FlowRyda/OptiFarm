package at.ac.tuwien.ase.groupphase.backend.service;

import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.CropDto;
import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.activefield.ActiveFieldCreateDto;
import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.activefield.ActiveFieldDetailDto;
import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.activefield.ActiveFieldUpdateDto;
import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.activefield.SimpleActiveFieldDto;
import at.ac.tuwien.ase.groupphase.backend.entity.Crop;
import at.ac.tuwien.ase.groupphase.backend.exception.ValidationException;

import java.util.List;

/**
 * Active Field service.
 */
public interface ActiveFieldService {

    /**
     * Creates a new active field with the provided details.
     *
     * @param activeField the data transfer object containing the active field details such as name, crop type, etc.
     * @return a simplified DTO representing the created active field
     */
    SimpleActiveFieldDto create(ActiveFieldCreateDto activeField) throws ValidationException;


    /**
     * Harvests the active field and then deletes it.
     *
     * @param crop          the data transfer object containing the crop details.
     * @param activeFieldId the ID of the active field to be harvested.
     * @return a crop object.
     */
    Crop harvestActiveField(CropDto crop, Long activeFieldId);

    /**
     * Retrieves all active fields.
     *
     * @return List of all active fields.
     */
    List<SimpleActiveFieldDto> getAllActiveFields();

    /**
     * Retrieves all active fields with no end date.
     *
     * @return List of all active fields with no end date.
     */
    List<SimpleActiveFieldDto> getAllActiveFieldsWithoutEndDate();

    /**
     * Updates an existing active field with the provided details.
     *
     * @param activeField the data transfer object containing the updated active field details.
     * @return a simplified DTO representing the updated active field
     */
    SimpleActiveFieldDto update(ActiveFieldUpdateDto activeField, Long id);

    /**
     * Retrieves an active field by its unique identifier.
     *
     * @param id the unique identifier for the field to be retrieved.
     * @return a detail DTO representation of the active field.
     */
    ActiveFieldDetailDto getActiveFieldById(Long id);

    /**
     * Retrieves an active field by the field id.
     *
     * @param fieldId the unique identifier for the field to be retrieved.
     * @return a detail DTO representation of the active field.
     */
    ActiveFieldDetailDto getActiveFieldByFieldId(Long fieldId);

    /**
     * Deletes an active field by its unique identifier.
     *
     * @param id the unique identifier for the active field to be deleted.
     */
    void deleteActiveFieldById(Long id);


}