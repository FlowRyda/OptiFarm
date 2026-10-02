package at.ac.tuwien.ase.groupphase.backend.service.impl;

import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.ToolCategoryDto;
import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.VehicleDto;
import at.ac.tuwien.ase.groupphase.backend.endpoint.mapper.VehicleMapper;
import at.ac.tuwien.ase.groupphase.backend.entity.*;
import at.ac.tuwien.ase.groupphase.backend.exception.AccessDeniedException;
import at.ac.tuwien.ase.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.ase.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.ase.groupphase.backend.repository.FarmerRepository;
import at.ac.tuwien.ase.groupphase.backend.repository.GarageRepository;
import at.ac.tuwien.ase.groupphase.backend.repository.VehicleRepository;
import at.ac.tuwien.ase.groupphase.backend.security.JwtUtil;
import at.ac.tuwien.ase.groupphase.backend.service.ToolCategoryService;
import at.ac.tuwien.ase.groupphase.backend.service.VehicleService;
import at.ac.tuwien.ase.groupphase.backend.service.validation.VehicleValidator;
import org.locationtech.jts.geom.Coordinate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.invoke.MethodHandles;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
public class VehicleServiceImpl implements VehicleService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final ToolCategoryService toolCategoryService;
    private final FarmerRepository farmerRepository;
    private final VehicleRepository vehicleRepository;
    private final GarageRepository garageRepository;
    private final VehicleMapper mapper;
    private final VehicleValidator validator;

    @Autowired
    public VehicleServiceImpl(ToolCategoryService toolCategoryService, FarmerRepository farmerRepository, VehicleRepository vehicleRepository,
                              GarageRepository garageRepository, VehicleMapper mapper, VehicleValidator validator) {
        this.toolCategoryService = toolCategoryService;
        this.farmerRepository = farmerRepository;
        this.vehicleRepository = vehicleRepository;
        this.garageRepository = garageRepository;
        this.mapper = mapper;
        this.validator = validator;
    }

    @Override
    @Transactional
    public VehicleDto createVehicle(VehicleDto dto) throws ValidationException {
        LOGGER.info("Creating vehicle {}", dto);
        var farmer = getFarmer();
        ToolCategory category = toolCategoryService.getOrCreateToolCategory(dto.getCategoryDto().getName());
        dto.setCategoryDto(new ToolCategoryDto(category.getId(), category.getName()));

        validator.validateForCreate(dto);

        var garage = garageRepository.getGarageByIdAndOwnerId(dto.getGarageDto().getId(), farmer.getId()).orElseThrow(() -> {
            LOGGER.info("Garage not found for vehicle update: {}", dto.getGarageDto().getId());
            return new NotFoundException(String.format("Garage with id %s for logged in user with id %s not found.", dto.getGarageDto().getId(), farmer.getId()));
        });

        final Vehicle vehicle = new Vehicle(
            dto.getName(),
            dto.getDescription(),
            dto.getRemainingValue(),
            category,
            garage,
            new Coordinate(dto.getLocationDto().getLat(), dto.getLocationDto().getLon()),
            dto.isUsable(),
            dto.getImage() == null ? null : dto.getImage().getBytes(StandardCharsets.UTF_8),
            dto.getFuelLevel(),
            dto.getDrivenKm()
        );

        vehicle.setOwnerId(farmer.getId());
        vehicle.setOwner(farmer);

        return mapper.mapVehicleEntityToDto(vehicleRepository.save(vehicle));
    }

    @Override
    @Transactional(readOnly = true)
    public VehicleDto getVehicleById(long id) throws NotFoundException, AccessDeniedException {
        LOGGER.info("Getting vehicle {}", id);
        var farmer = getFarmer();

        Vehicle vehicle = vehicleRepository.findById(id).orElseThrow(() -> {
            LOGGER.info("Vehicle with id {} not found", id);
            return new NotFoundException(String.format("Could not find a vehicle with the id %s", id));
        });

        if (!vehicle.getOwner().getId().equals(farmer.getId())) {
            LOGGER.info("farmer id {} not allowed to view vehicle owned by {}", farmer.getId(), vehicle.getOwner().getId());
            throw new AccessDeniedException("You do not have permission to view this vehicle.");
        }

        return mapper.mapVehicleEntityToDto(vehicle);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VehicleDto> getAllVehicles() {
        LOGGER.info("Getting all vehicles for logged in user");
        var farmer = getFarmer();

        return vehicleRepository.findAllByOwner(farmer).stream()
            .map(mapper::mapVehicleEntityToDto)
            .collect(java.util.stream.Collectors.toList());
    }

    @Override
    @Transactional
    public VehicleDto updateVehicle(long id, VehicleDto dto) throws NotFoundException, AccessDeniedException, ValidationException {
        LOGGER.info("Updating vehicle with id {}: {}", id, dto);
        var farmer = getFarmer();
        ToolCategory category = toolCategoryService.getOrCreateToolCategory(dto.getCategoryDto().getName());
        dto.setCategoryDto(new ToolCategoryDto(category.getId(), category.getName()));

        validator.validateForUpdate(dto);

        Vehicle vehicle = vehicleRepository.findById(id).orElseThrow(() -> {
            LOGGER.info("Vehicle with id {} not found for update", id);
            return new NotFoundException(String.format("Could not find a vehicle with the id %s", id));
        });

        if (!vehicle.getOwner().getId().equals(farmer.getId())) {
            LOGGER.info("farmer id {} not allowed to update vehicle owned by {}", farmer.getId(), vehicle.getOwner().getId());
            throw new AccessDeniedException("You do not have permission to update this vehicle.");
        }

        var garage = garageRepository.getGarageByIdAndOwnerId(dto.getGarageDto().getId(), farmer.getId()).orElseThrow(() -> {
            LOGGER.info("Garage not found for update: {}", dto.getGarageDto().getId());
            return new NotFoundException(String.format("Garage with id %s for logged in user with id %s not found.", dto.getGarageDto().getId(), farmer.getId()));
        });

        vehicle.setName(dto.getName());
        vehicle.setDescription(dto.getDescription());
        vehicle.setCategory(category);
        vehicle.setHomeGarage(garage);
        vehicle.setCoordinate(new Coordinate(dto.getLocationDto().getLat(), dto.getLocationDto().getLon()));
        vehicle.setRemainingValue(dto.getRemainingValue());
        vehicle.setUsable(dto.isUsable());
        vehicle.setImage(dto.getImage() == null ? null : dto.getImage().getBytes(StandardCharsets.UTF_8));
        vehicle.setFuelLevel(dto.getFuelLevel());
        vehicle.setDrivenKm(dto.getDrivenKm());

        return mapper.mapVehicleEntityToDto(vehicleRepository.save(vehicle));
    }

    @Override
    @Transactional
    public void deleteVehicle(long id) throws NotFoundException, AccessDeniedException {
        LOGGER.info("Deleting vehicle with id {}", id);
        var farmer = getFarmer();

        Vehicle vehicle = vehicleRepository.findById(id).orElseThrow(() -> {
            LOGGER.info("Vehicle with id {} not found for delete", id);
            return new NotFoundException(String.format("Could not find a vehicle with the id %s", id));
        });

        if (!vehicle.getOwner().getId().equals(farmer.getId())) {
            LOGGER.info("farmer id {} not allowed to delete vehicle", farmer.getId());
            throw new AccessDeniedException("You do not have permission to delete this vehicle.");
        }

        vehicleRepository.delete(vehicle);
    }

    private Farmer getFarmer() throws NotFoundException {
        var activeUser = JwtUtil.getActiveJwtUser();

        return farmerRepository.findById(activeUser.id()).orElseThrow(() -> {
            LOGGER.info("Farmer with id {} not found", activeUser.id());
            return new NotFoundException(String.format("Could not find a farmer with the id %s", activeUser.id()));
        });
    }
}
