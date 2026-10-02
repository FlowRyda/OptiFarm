package at.ac.tuwien.ase.groupphase.backend.service.impl;

import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.locator.LocatedObjectDto;
import at.ac.tuwien.ase.groupphase.backend.endpoint.mapper.LocatedObjectMapper;
import at.ac.tuwien.ase.groupphase.backend.entity.Farmer;
import at.ac.tuwien.ase.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.ase.groupphase.backend.repository.*;
import at.ac.tuwien.ase.groupphase.backend.security.JwtUtil;
import at.ac.tuwien.ase.groupphase.backend.service.LocatorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class LocatorServiceImpl implements LocatorService {

    private final FarmerRepository farmerRepository;
    private final ToolRepository toolRepository;
    private final FieldRepository fieldRepository;
    private final GarageRepository garageRepository;
    private final ActiveFieldRepository activeFieldRepository;

    @Autowired
    public LocatorServiceImpl(FarmerRepository farmerRepository, ToolRepository toolRepository, FieldRepository fieldRepository, GarageRepository garageRepository, ActiveFieldRepository activeFieldRepository) {
        this.farmerRepository = farmerRepository;
        this.toolRepository = toolRepository;
        this.fieldRepository = fieldRepository;
        this.garageRepository = garageRepository;
        this.activeFieldRepository = activeFieldRepository;
    }

    @Override
    public List<LocatedObjectDto> getLocatedObjectsFromFarmer() {
        var owner = getOwner();

        var tools = toolRepository.findAllByOwner(owner);
        var activeFields = activeFieldRepository.findAllByOwnerAndEndDateIsNull(owner);
        var garages = garageRepository.findAllByOwner(owner);

        var activeFieldIds = activeFields.stream()
            .map(x -> x.getField().getId())
            .toList();

        var fields = activeFieldIds.isEmpty()
            ? fieldRepository.findAllByOwner(owner)
            : fieldRepository.findAllByOwnerAndIdNotIn(owner, activeFieldIds);

        var result = new ArrayList<LocatedObjectDto>();

        tools.stream()
            .map(LocatedObjectMapper::toDto)
            .forEach(result::add);

        garages.stream().map(LocatedObjectMapper::toDto).forEach(result::add);

        activeFields.stream()
            .map(LocatedObjectMapper::toDto)
            .forEach(result::add);

        fields.stream()
            .map(LocatedObjectMapper::toDto)
            .forEach(result::add);

        return result;
    }

    private Farmer getOwner() {
        var ownerId = JwtUtil.getActiveJwtUser().id();
        return farmerRepository.findById(ownerId).orElseThrow(() -> new NotFoundException("Owner not found"));
    }
}
