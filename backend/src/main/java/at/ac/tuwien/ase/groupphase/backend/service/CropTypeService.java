package at.ac.tuwien.ase.groupphase.backend.service;

import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.CropTypeDto;
import at.ac.tuwien.ase.groupphase.backend.exception.ValidationException;

import java.util.List;

/**
 * Service interface for managing CropType entities.
 */
public interface CropTypeService {

    /**
     * Retrieves all crop types.
     *
     * @return a list of CropTypeDto representing all crop types
     */
    List<CropTypeDto> findAll();

    /**
     * Retrieves a crop type by its id.
     *
     * @param id the id of the crop type
     * @return the CropTypeDto with the given id
     */
    CropTypeDto findById(Long id);

    /**
     * Creates a new crop type.
     *
     * @param dto the CropTypeDto to create
     * @return the created CropTypeDto with generated id
     */
    CropTypeDto create(CropTypeDto dto) throws ValidationException;

    /**
     * Updates an existing crop type by id.
     *
     * @param id  the id of the crop type to update
     * @param dto the CropTypeDto containing updated data
     * @return the updated CropTypeDto
     */
    CropTypeDto update(Long id, CropTypeDto dto) throws ValidationException;

    /**
     * Deletes a crop type by its id.
     *
     * @param id the id of the crop type to delete
     */
    void delete(Long id);
}
