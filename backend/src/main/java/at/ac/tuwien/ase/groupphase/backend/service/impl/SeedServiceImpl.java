package at.ac.tuwien.ase.groupphase.backend.service.impl;

import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.SeedDto;
import at.ac.tuwien.ase.groupphase.backend.endpoint.mapper.SeedMapper;
import at.ac.tuwien.ase.groupphase.backend.entity.CropType;
import at.ac.tuwien.ase.groupphase.backend.entity.Farmer;
import at.ac.tuwien.ase.groupphase.backend.entity.Seed;
import at.ac.tuwien.ase.groupphase.backend.exception.AccessDeniedException;
import at.ac.tuwien.ase.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.ase.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.ase.groupphase.backend.repository.CropTypeRepository;
import at.ac.tuwien.ase.groupphase.backend.repository.FarmerRepository;
import at.ac.tuwien.ase.groupphase.backend.repository.SeedRepository;
import at.ac.tuwien.ase.groupphase.backend.security.JwtUtil;
import at.ac.tuwien.ase.groupphase.backend.service.SeedService;
import at.ac.tuwien.ase.groupphase.backend.service.validation.SeedValidator;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Transactional
public class SeedServiceImpl implements SeedService {
    private final FarmerRepository farmerRepository;
    private final SeedRepository seedRepository;
    private final CropTypeRepository cropTypeRepository;
    private final SeedMapper mapper;
    private final SeedValidator validator;
    private static final Logger logger = LoggerFactory.getLogger(SeedServiceImpl.class);

    public SeedServiceImpl(SeedRepository seedRepository, SeedMapper mapper,
                           CropTypeRepository cropTypeRepository,
                           FarmerRepository farmerRepository, SeedValidator validator) {
        this.farmerRepository = farmerRepository;
        this.seedRepository = seedRepository;
        this.mapper = mapper;
        this.cropTypeRepository = cropTypeRepository;
        this.validator = validator;
    }

    @Override
    public Seed saveSeed(Seed seed) throws ValidationException {
        var farmer = getFarmer();
        SeedDto dto = mapper.toDto(seed);

        List<Seed> existingSeeds = seedRepository.findAllByOwner(farmer);
        validator.validateForCreate(dto, existingSeeds);

        seed.setOwner(farmer);
        seed.setOwnerId(farmer.getId());

        logger.info("Saving seed: {}", seed);
        Seed saved = seedRepository.save(seed);
        logger.info("Saved seed with id {}", saved.getId());
        return saved;
    }

    @Override
    public Seed getSeedById(Long id) {
        logger.info("Fetching seed by id {}", id);
        var farmer = getFarmer();

        Seed seed = seedRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Seed not found"));

        if (!Objects.equals(seed.getOwner().getId(), farmer.getId())) {
            logger.info("Farmer {} not allowed to view seed owned by {}", farmer.getId(), seed.getOwner().getId());
            throw new AccessDeniedException("Not allowed to view this seed");
        }

        return seed;
    }

    @Override
    public List<SeedDto> getAllSeeds() {
        logger.info("Fetching all seeds");
        var farmer = getFarmer();

        List<SeedDto> seeds = seedRepository.findAllByOwner(farmer)
            .stream()
            .map(SeedMapper::toDto)
            .collect(Collectors.toList());

        logger.info("Found {} seeds", seeds.size());
        return seeds;
    }

    @Override
    public SeedDto update(Long id, SeedDto dto) throws ValidationException {
        logger.info("Updating seed with id {}: {}", id, dto);
        var farmer = getFarmer();

        Seed entity = seedRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Seed not found: " + id));

        if (!entity.getOwner().equals(farmer)) {
            throw new AccessDeniedException("You are not allowed to edit this seed");
        }

        List<Seed> existingSeeds = seedRepository.findAllByOwner(farmer);
        validator.validateForUpdate(dto, existingSeeds, id);

        CropType cropEntity = cropTypeRepository.findById(dto.getCropTypeId())
            .orElseThrow(() -> {
                logger.error("CropType not found for seed update: {}", dto.getCropTypeId());
                return new RuntimeException("CropType not found for seed: " + id);
            });
        mapper.updateEntityFromDto(dto, entity, cropEntity);
        Seed saved = seedRepository.save(entity);

        logger.info("Updated seed with id {}", saved.getId());
        return SeedMapper.toDto(saved);
    }

    @Override
    public void deleteSeed(Long id) {
        logger.info("Deleting seed with id {}", id);

        Seed existingSeed = seedRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Seed not found"));

        if (!existingSeed.getOwner().equals(getFarmer())) {
            throw new AccessDeniedException("You are not allowed to delete this seed");
        }

        seedRepository.deleteById(id);
        logger.info("Deleted seed with id {}", id);
    }

    @Override
    public List<Seed> getSeedsByCropType(Long cropTypeId) {
        logger.info("Fetching seeds by crop type id {}", cropTypeId);
        var farmer = getFarmer();

        CropType cropEntity = cropTypeRepository.findById(cropTypeId)
            .orElseThrow(() -> new NotFoundException("CropType not found for Id: " + cropTypeId));

        if (!cropEntity.getOwner().equals(farmer)) {
            throw new AccessDeniedException("You are not allowed to get seeds for this crop type");
        }

        List<Seed> seeds = seedRepository.findByTypeId(cropTypeId);

        logger.info("Found {} seeds for crop type id {}", seeds.size(), cropTypeId);
        return seeds;
    }

    private Farmer getFarmer() {
        var activeUser = JwtUtil.getActiveJwtUser();
        return farmerRepository.findById(activeUser.id())
            .orElseThrow(() -> new NotFoundException(
                String.format("Could not find a farmer with the id %s", activeUser.id())
            ));
    }
}
