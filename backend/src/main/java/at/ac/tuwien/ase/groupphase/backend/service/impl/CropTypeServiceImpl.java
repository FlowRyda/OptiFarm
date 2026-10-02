package at.ac.tuwien.ase.groupphase.backend.service.impl;

import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.CropTypeDto;
import at.ac.tuwien.ase.groupphase.backend.endpoint.mapper.CropTypeMapper;
import at.ac.tuwien.ase.groupphase.backend.entity.CropType;
import at.ac.tuwien.ase.groupphase.backend.entity.Farmer;
import at.ac.tuwien.ase.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.ase.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.ase.groupphase.backend.repository.CropTypeRepository;
import at.ac.tuwien.ase.groupphase.backend.repository.FarmerRepository;
import at.ac.tuwien.ase.groupphase.backend.security.JwtUtil;
import at.ac.tuwien.ase.groupphase.backend.service.CropTypeService;
import at.ac.tuwien.ase.groupphase.backend.service.validation.CropTypeValidator;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
public class CropTypeServiceImpl implements CropTypeService {

    private static final Logger log = LoggerFactory.getLogger(CropTypeServiceImpl.class);

    private final CropTypeRepository repo;
    private final FarmerRepository farmerRepository;
    private final CropTypeMapper mapper;
    private final CropTypeValidator validator;

    public CropTypeServiceImpl(CropTypeRepository repo, FarmerRepository farmerRepository, CropTypeMapper mapper, CropTypeValidator validator) {
        this.repo = repo;
        this.farmerRepository = farmerRepository;
        this.mapper = mapper;
        this.validator = validator;
    }

    private Farmer getLoggedInFarmer() {
        var user = JwtUtil.getActiveJwtUser();
        return farmerRepository.findById(user.id()).orElseThrow(
            () -> new NotFoundException("Farmer not found: " + user.id())
        );
    }

    @Override
    public List<CropTypeDto> findAll() {
        var farmer = getLoggedInFarmer();
        log.info("Fetching Crops for Farmer {}", farmer.getId());

        return repo.findAllByOwner(farmer)
            .stream()
            .map(mapper::toDto)
            .toList();
    }

    @Override
    public CropTypeDto findById(Long id) {
        var farmer = getLoggedInFarmer();
        log.info("Fetching CropType {} for Farmer {}", id, farmer.getId());

        var entity = repo.findByIdAndOwnerId(id, farmer.getId())
            .orElseThrow(() -> new NotFoundException(
                "CropType " + id + " not found for user " + farmer.getId()
            ));

        return mapper.toDto(entity);
    }

    @Override
    public CropTypeDto create(CropTypeDto dto) throws ValidationException {
        var farmer = getLoggedInFarmer();
        log.info("Creating CropType for Farmer {}", farmer.getId());

        List<CropType> existing = repo.findAllByOwner(farmer);
        validator.validateForCreate(dto, existing);

        CropType entity = mapper.toEntity(dto);
        entity.setOwner(farmer);
        entity.setOwnerId(farmer.getId());
        entity.setId(null);

        var saved = repo.save(entity);
        return mapper.toDto(saved);
    }

    @Override
    public CropTypeDto update(Long id, CropTypeDto dto) throws ValidationException {
        var farmer = getLoggedInFarmer();
        log.info("Updating CropType {} for Farmer {}", id, farmer.getId());

        List<CropType> existing = repo.findAllByOwner(farmer);
        validator.validateForUpdate(dto, existing, id);

        var entity = repo.findByIdAndOwnerId(id, farmer.getId())
            .orElseThrow(() -> new NotFoundException(
                "CropType " + id + " not found for user " + farmer.getId()
            ));

        mapper.updateEntityFromDto(dto, entity);

        var saved = repo.save(entity);
        return mapper.toDto(saved);
    }

    @Override
    public void delete(Long id) {
        var farmer = getLoggedInFarmer();
        log.info("Deleting CropType {} for Farmer {}", id, farmer.getId());

        repo.findByIdAndOwnerId(id, farmer.getId())
            .orElseThrow(() -> new NotFoundException(
                "CropType " + id + " not found for user " + farmer.getId()
            ));

        repo.deleteByIdAndOwnerId(id, farmer.getId());
    }
}
