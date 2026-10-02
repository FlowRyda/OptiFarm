package at.ac.tuwien.ase.groupphase.backend.service;

import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.VehicleDto;
import at.ac.tuwien.ase.groupphase.backend.exception.AccessDeniedException;
import at.ac.tuwien.ase.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.ase.groupphase.backend.exception.ValidationException;

import java.util.List;

/**
 * Service for managing vehicles and CRUD operations.
 */
public interface VehicleService {

    /**
     * Creates a new vehicle.
     *
     * @param dto the vehicle data transfer object
     * @return the created vehicle as DTO
     * @throws ValidationException if the provided data is invalid
     */
    VehicleDto createVehicle(VehicleDto dto) throws ValidationException;

    /**
     * Retrieves a vehicle by its ID.
     *
     * @param id the vehicle ID
     * @return the corresponding vehicle DTO
     * @throws NotFoundException     if no vehicle with the given ID exists
     * @throws AccessDeniedException if the current user is not allowed to access this vehicle
     */
    VehicleDto getVehicleById(long id) throws NotFoundException, AccessDeniedException;

    /**
     * Returns all vehicles.
     *
     * @return list of all vehicle DTOs
     */
    List<VehicleDto> getAllVehicles();

    /**
     * Updates an existing vehicle.
     *
     * @param id  the ID of the vehicle to update
     * @param dto updated vehicle data
     * @return the updated vehicle DTO
     * @throws NotFoundException     if the vehicle does not exist
     * @throws AccessDeniedException if the user is not allowed to modify this vehicle
     * @throws ValidationException   if the updated data is invalid
     */
    VehicleDto updateVehicle(long id, VehicleDto dto)
        throws NotFoundException, AccessDeniedException, ValidationException;

    /**
     * Deletes a vehicle by its ID.
     *
     * @param id the vehicle ID
     * @throws NotFoundException     if the vehicle does not exist
     * @throws AccessDeniedException if the user is not allowed to delete this vehicle
     */
    void deleteVehicle(long id) throws NotFoundException, AccessDeniedException;
}
