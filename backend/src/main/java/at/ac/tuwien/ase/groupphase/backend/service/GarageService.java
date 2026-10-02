package at.ac.tuwien.ase.groupphase.backend.service;

import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.garage.GarageDto;

import java.util.List;

public interface GarageService {

    GarageDto getGarageOfLoggedInFarmerById(Long id);

    List<GarageDto> getAllGaragesOfLoggedInFarmerById();

    GarageDto createGarage(GarageDto garageDto);

    void delete(Long id);

    GarageDto updateGarage(Long id, GarageDto garageDto) throws Exception;
}
