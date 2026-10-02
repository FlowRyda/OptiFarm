package at.ac.tuwien.ase.groupphase.backend.service;

import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.SeedDto;
import at.ac.tuwien.ase.groupphase.backend.entity.Seed;
import at.ac.tuwien.ase.groupphase.backend.exception.ValidationException;

import java.util.List;

/**
 * Service interface for managing Seed entities.
 */
public interface SeedService {

    /**
     * Saves a seed entity to the database.
     *
     * @param seed the Seed entity to save
     * @return the saved Seed with generated id
     */
    Seed saveSeed(Seed seed) throws ValidationException;

    /**
     * Retrieves a seed by its id.
     *
     * @param id the id of the seed
     * @return the Seed with the given id
     * @throws RuntimeException if no seed is found with the given id
     */
    Seed getSeedById(Long id);

    /**
     * Retrieves all seeds as DTOs.
     *
     * @return a list of SeedDto representing all seeds
     */
    List<SeedDto> getAllSeeds();

    /**
     * Updates an existing seed.
     *
     * @param id  the id of the seed to update
     * @param dto the SeedDto containing updated data
     * @return the updated SeedDto
     * @throws RuntimeException if no seed or crop type is found for the update
     */
    SeedDto update(Long id, SeedDto dto) throws ValidationException;

    /**
     * Deletes a seed by its id.
     *
     * @param id the id of the seed to delete
     */
    void deleteSeed(Long id);

    /**
     * Retrieves all seeds associated with a specific crop type.
     *
     * @param cropTypeId the id of the crop type
     * @return a list of Seed entities belonging to the given crop type
     */
    List<Seed> getSeedsByCropType(Long cropTypeId);
}
