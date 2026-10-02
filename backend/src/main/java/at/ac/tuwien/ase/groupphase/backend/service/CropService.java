package at.ac.tuwien.ase.groupphase.backend.service;

import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.CropDetailDto;
import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.CropDto;
import at.ac.tuwien.ase.groupphase.backend.entity.Crop;

import java.util.List;

/**
 * Service interface for managing Crop entities.
 */
public interface CropService {

    /**
     * Saves a crop entity to the database.
     *
     * @param crop the crop to save
     * @return the saved crop with generated id
     */
    Crop saveCrop(CropDto crop);

    /**
     * Retrieves a crop by its id.
     *
     * @param id the id of the crop
     * @return the crop with the given id
     * @throws RuntimeException if no crop is found with the given id
     */
    Crop getCropById(Long id);

    /**
     * Retrieves all crops from the database.
     *
     * @return a list of all crops
     */
    List<Crop> getAllCrops();

    /**
     * Deletes a crop by its id.
     *
     * @param id the id of the crop to delete
     */
    void deleteCrop(Long id);

    /**
     * Retrieves all crops belonging to a specific crop type.
     *
     * @param cropTypeId the id of the crop type
     * @return a list of crops with the given crop type
     */
    List<Crop> getCropsByCropType(Long cropTypeId);

    /**
     * Retrieves all crops associated with a specific field.
     *
     * @param fieldId the id of the field
     * @return a list of crops with the given field
     */
    List<CropDetailDto> getCropsByField(Long fieldId);
}
