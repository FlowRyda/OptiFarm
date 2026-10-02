package at.ac.tuwien.ase.groupphase.backend.service.impl;

import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.CropDto;
import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.activefield.ActiveFieldCreateDto;
import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.activefield.ActiveFieldDetailDto;
import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.activefield.ActiveFieldUpdateDto;
import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.activefield.SimpleActiveFieldDto;
import at.ac.tuwien.ase.groupphase.backend.endpoint.mapper.ActiveFieldMapper;
import at.ac.tuwien.ase.groupphase.backend.entity.*;
import at.ac.tuwien.ase.groupphase.backend.exception.AccessDeniedException;
import at.ac.tuwien.ase.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.ase.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.ase.groupphase.backend.repository.*;
import at.ac.tuwien.ase.groupphase.backend.security.JwtUtil;
import at.ac.tuwien.ase.groupphase.backend.service.ActiveFieldService;
import at.ac.tuwien.ase.groupphase.backend.service.CropService;
import at.ac.tuwien.ase.groupphase.backend.service.validation.ActiveFieldValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.invoke.MethodHandles;
import java.util.List;

@Service
public class ActiveFieldServiceImpl implements ActiveFieldService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final ActiveFieldRepository activeFieldRepository;

    private final ActiveFieldMapper activeFieldMapper;
    private final FarmerRepository farmerRepository;
    private final CropTypeRepository cropTypeRepository;

    private final FieldRepository fieldRepository;
    private final SeedRepository seedRepository;
    private final CropService cropService;
    private final ActiveFieldValidator validator;

    public ActiveFieldServiceImpl(CropService cropService, ActiveFieldRepository activeFieldRepository, ActiveFieldMapper activeFieldMapper, FarmerRepository farmerRepository,
                                  CropTypeRepository cropTypeRepository, FieldRepository fieldRepository, SeedRepository seedRepository, ActiveFieldValidator validator) {
        this.activeFieldMapper = activeFieldMapper;
        this.farmerRepository = farmerRepository;
        this.activeFieldRepository = activeFieldRepository;
        this.cropTypeRepository = cropTypeRepository;
        this.fieldRepository = fieldRepository;
        this.seedRepository = seedRepository;
        this.cropService = cropService;
        this.validator = validator;
    }

    @Override
    @Transactional
    public SimpleActiveFieldDto create(ActiveFieldCreateDto fieldDto) throws ValidationException {
        LOGGER.info("Saving new active field with id {}", fieldDto.fieldId());

        Farmer owner = getFarmer();
        validator.validateCreate(fieldDto, owner);

        Seed seed = seedRepository.findById(fieldDto.cropTypeId()).get();
        Field field = fieldRepository.findById(fieldDto.fieldId()).get();

        ActiveField activeField = new ActiveField();
        activeField.setOwner(owner);
        activeField.setStartDate(fieldDto.startDate());
        activeField.setOwnerId(owner.getId());
        activeField.setSeed(seed);
        activeField.setField(field);
        activeField.setExpectedHarvestDate(fieldDto.expectedHarvestDate());
        activeField.setSellingPrice(fieldDto.sellingPrice());

        ActiveField savedField = activeFieldRepository.save(activeField);
        LOGGER.info("Saved active field with id {}", savedField.getId());

        return activeFieldMapper.toSimpleDto(savedField);
    }

    @Override
    @Transactional
    public SimpleActiveFieldDto update(ActiveFieldUpdateDto activeField, Long id) {
        LOGGER.info("Updating active field with id {}", activeField.fieldId());
        ActiveField existingField = activeFieldRepository.findById(id).orElseThrow(() -> new NotFoundException("Active Field not found"));

        if (!existingField.getOwner().equals(getFarmer())) {
            throw new AccessDeniedException("You are not allowed to edit this field");
        }
        existingField.setEndDate(activeField.endDate());
        existingField.setExpectedHarvestDate(activeField.expectedDate());
        ActiveField savedField = activeFieldRepository.save(existingField);
        LOGGER.info("Updated active field with id {}", savedField.getId());
        return activeFieldMapper.toSimpleDto(savedField);
    }

    @Override
    @Transactional
    public ActiveFieldDetailDto getActiveFieldById(Long id) {
        LOGGER.info("Getting active field with id {}", id);
        ActiveField activeField = activeFieldRepository.findById(id).orElseThrow(() -> new NotFoundException("Active Field not found"));
        if (!activeField.getOwner().equals(getFarmer())) {
            throw new AccessDeniedException("You are not allowed to fetch this active field");
        }
        return activeFieldMapper.toDetailDto(activeField);
    }

    @Override
    @Transactional
    public ActiveFieldDetailDto getActiveFieldByFieldId(Long id) {
        LOGGER.info("Getting field for active field with id {}", id);
        Field field = fieldRepository.findById(id).orElseThrow(() -> new NotFoundException("Field not found"));


        ActiveField activeField = activeFieldRepository.findByFieldIdAndEndDateIsNull(field.getId()).orElseThrow(() -> new NotFoundException("Active Field not found"));
        if (!activeField.getOwner().equals(getFarmer())) {
            throw new AccessDeniedException("You are not allowed to fetch this active field");
        }
        return activeFieldMapper.toDetailDto(activeField);
    }

    @Override
    @Transactional
    public void deleteActiveFieldById(Long id) {
        LOGGER.info("Deleting active field with id {}", id);
        ActiveField activeField = activeFieldRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Active Field not found"));

        if (!activeField.getOwner().equals(getFarmer())) {
            throw new AccessDeniedException("You are not allowed to delete this active field");
        }
        activeFieldRepository.delete(activeField);
        LOGGER.info("Deleted active field with id {}", id);
    }

    @Override
    @Transactional
    public List<SimpleActiveFieldDto> getAllActiveFields() {
        LOGGER.info("Getting all active fields");

        Farmer owner = getFarmer();
        return activeFieldMapper.listToSimpleDtoList(activeFieldRepository.findAllByOwner(owner));
    }

    @Override
    @Transactional
    public List<SimpleActiveFieldDto> getAllActiveFieldsWithoutEndDate() {
        LOGGER.info("Getting all active fields without end date");

        Farmer owner = getFarmer();
        return activeFieldMapper.listToSimpleDtoList(activeFieldRepository.findAllByOwnerAndEndDateIsNull(owner));
    }

    private Farmer getFarmer() throws NotFoundException {
        var activeUser = JwtUtil.getActiveJwtUser();

        return farmerRepository.findById(activeUser.id()).orElseThrow(() ->
            new NotFoundException(String.format("Could not find a farmer with the id %s", activeUser.id())));
    }

    @Transactional
    public Crop harvestActiveField(CropDto crop, Long activeFieldId) {
        LOGGER.info("Harvesting and then deleting active field)");

        Crop saved = cropService.saveCrop(crop);
        deleteActiveFieldById(activeFieldId);

        return saved;
    }

}