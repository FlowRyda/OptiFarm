package at.ac.tuwien.ase.groupphase.backend.service.impl;

import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.CropDetailDto;
import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.CropDto;
import at.ac.tuwien.ase.groupphase.backend.endpoint.mapper.CropMapper;
import at.ac.tuwien.ase.groupphase.backend.entity.Crop;
import at.ac.tuwien.ase.groupphase.backend.entity.CropType;
import at.ac.tuwien.ase.groupphase.backend.entity.Farmer;
import at.ac.tuwien.ase.groupphase.backend.entity.Field;
import at.ac.tuwien.ase.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.ase.groupphase.backend.repository.CropRepository;
import at.ac.tuwien.ase.groupphase.backend.repository.CropTypeRepository;
import at.ac.tuwien.ase.groupphase.backend.repository.FarmerRepository;
import at.ac.tuwien.ase.groupphase.backend.repository.FieldRepository;
import at.ac.tuwien.ase.groupphase.backend.security.JwtUtil;
import at.ac.tuwien.ase.groupphase.backend.service.CropService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
public class CropServiceImpl implements CropService {

    private static final Logger logger = LoggerFactory.getLogger(CropServiceImpl.class);
    private final CropRepository cropRepository;
    private final FarmerRepository farmerRepository;
    private final CropTypeRepository cropTypeRepository;
    private final FieldRepository fieldRepository;

    @Autowired
    public CropServiceImpl(CropRepository cropRepository, FarmerRepository farmerRepository, CropTypeRepository cropTypeRepository, FieldRepository fieldRepository) {
        this.cropRepository = cropRepository;
        this.farmerRepository = farmerRepository;
        this.cropTypeRepository = cropTypeRepository;
        this.fieldRepository = fieldRepository;
    }

    private Farmer getLoggedInFarmer() {
        var user = JwtUtil.getActiveJwtUser();
        return farmerRepository.findById(user.id()).orElseThrow(
            () -> new NotFoundException("Farmer not found: " + user.id())
        );
    }

    public Crop saveCrop(CropDto dto) {
        Farmer farmer = farmerRepository.findById(dto.getFarmerId())
            .orElseThrow(() -> {
                logger.error("Farmer with id {} not found", dto.getFarmerId());
                return new RuntimeException("Farmer not found");
            });

        CropType cropType = cropTypeRepository.findById(dto.getCropTypeId())
            .orElseThrow(() -> {
                logger.error("CropType with id {} not found", dto.getCropTypeId());
                return new RuntimeException("CropType not found");
            });

        Field field = fieldRepository.findById(dto.getFieldId())
            .orElseThrow(() -> {
                logger.error("Field with id {} not found", dto.getFieldId());
                return new RuntimeException("Field not found");
            });

        Crop crop = CropMapper.toEntity(dto, farmer, cropType, field);
        crop.setOwnerId(farmer.getId());
        logger.info("Saving crop: {}", crop);
        Crop saved = cropRepository.save(crop);
        logger.info("Saved crop with id {}", saved.getId());
        return saved;
    }

    public Crop getCropById(Long id) {
        logger.info("Fetching crop by id {}", id);
        return cropRepository.findById(id)
            .orElseThrow(() -> {
                logger.error("Crop not found with id {}", id);
                return new RuntimeException("Crop not found");
            });
    }

    public List<Crop> getAllCrops() {
        logger.info("Fetching all crops");
        List<Crop> crops = cropRepository.findAll();
        logger.info("Found {} crops", crops.size());
        return crops;
    }

    public void deleteCrop(Long id) {
        logger.info("Deleting crop with id {}", id);
        cropRepository.deleteById(id);
        logger.info("Deleted crop with id {}", id);
    }

    public List<Crop> getCropsByCropType(Long cropTypeId) {
        logger.info("Fetching crops by crop type id {}", cropTypeId);
        List<Crop> crops = cropRepository.findByTypeId(cropTypeId);
        logger.info("Found {} crops for crop type id {}", crops.size(), cropTypeId);
        return crops;
    }

    public List<CropDetailDto> getCropsByField(Long fieldId) {
        logger.info("Fetching crops by field id {}", fieldId);

        Farmer owner = getLoggedInFarmer();
        Field existingField = fieldRepository.findById(fieldId).orElseThrow(() -> new NotFoundException("Field not found"));
        List<Crop> crops = cropRepository.findByFieldAndOwner(existingField, owner);

        return CropMapper.toDetailDtoList(crops);
    }
}
