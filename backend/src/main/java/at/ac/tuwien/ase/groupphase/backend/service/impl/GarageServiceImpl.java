package at.ac.tuwien.ase.groupphase.backend.service.impl;

import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.garage.GarageDto;
import at.ac.tuwien.ase.groupphase.backend.entity.Garage;
import at.ac.tuwien.ase.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.ase.groupphase.backend.repository.GarageRepository;
import at.ac.tuwien.ase.groupphase.backend.security.JwtUtil;
import at.ac.tuwien.ase.groupphase.backend.service.GarageService;
import org.locationtech.jts.geom.Coordinate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GarageServiceImpl implements GarageService {

    private final GarageRepository garageRepository;

    @Autowired
    public GarageServiceImpl(GarageRepository garageRepository) {
        this.garageRepository = garageRepository;
    }

    @Override
    public GarageDto getGarageOfLoggedInFarmerById(Long id) {
        var ownerId = JwtUtil.getActiveJwtUser().id();

        var garage = garageRepository.getGarageByIdAndOwnerId(id, ownerId).orElseThrow(
            () -> new NotFoundException(String.format("Garage with id %s for logged in user with id %s not found.", id, ownerId))
        );

        var garageDto = new GarageDto(garage);
        garageDto.loadTools(garage);

        return garageDto;
    }

    @Override
    public List<GarageDto> getAllGaragesOfLoggedInFarmerById() {
        var ownerId = JwtUtil.getActiveJwtUser().id();
        return garageRepository.getAllGaragesByOwnerId(ownerId).stream().map(GarageDto::new).toList();
    }

    @Override
    public GarageDto createGarage(GarageDto garageDto) {
        var ownerId = JwtUtil.getActiveJwtUser().id();

        var newGarage = new Garage();
        newGarage.setOwnerId(ownerId);
        newGarage.setName(garageDto.getName());

        var coordinateDto = garageDto.getCoordinate();
        newGarage.setCoordinate(new Coordinate(coordinateDto.getLat(), coordinateDto.getLon()));

        var result = garageRepository.save(newGarage);
        return new GarageDto(result);
    }

    @Override
    public void delete(Long id) {
        var ownerId = JwtUtil.getActiveJwtUser().id();
        var garageToDelete = garageRepository.getGarageByIdAndOwnerId(id, ownerId).orElseThrow();
        garageRepository.deleteById(garageToDelete.getId());
    }

    @Override
    public GarageDto updateGarage(Long id, GarageDto garageDto) throws Exception {
        var ownerId = JwtUtil.getActiveJwtUser().id();
        var garage = garageRepository.getGarageByIdAndOwnerId(id, ownerId).orElseThrow();
        garage.setName(garageDto.getName());
        var coordinateDto = garageDto.getCoordinate();
        garage.setCoordinate(new Coordinate(coordinateDto.getLat(), coordinateDto.getLon()));
        var saved = garageRepository.save(garage);

        return new GarageDto(saved);
    }
}
