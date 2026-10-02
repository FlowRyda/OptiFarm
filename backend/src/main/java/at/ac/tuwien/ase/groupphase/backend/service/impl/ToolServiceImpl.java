package at.ac.tuwien.ase.groupphase.backend.service.impl;

import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.*;
import at.ac.tuwien.ase.groupphase.backend.endpoint.mapper.ToolMapper;
import at.ac.tuwien.ase.groupphase.backend.entity.Farmer;
import at.ac.tuwien.ase.groupphase.backend.entity.Tool;
import at.ac.tuwien.ase.groupphase.backend.entity.ToolCategory;
import at.ac.tuwien.ase.groupphase.backend.exception.AccessDeniedException;
import at.ac.tuwien.ase.groupphase.backend.exception.InvalidTokenException;
import at.ac.tuwien.ase.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.ase.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.ase.groupphase.backend.repository.FarmerRepository;
import at.ac.tuwien.ase.groupphase.backend.repository.GarageRepository;
import at.ac.tuwien.ase.groupphase.backend.repository.ToolRepository;
import at.ac.tuwien.ase.groupphase.backend.security.JwtUtil;
import at.ac.tuwien.ase.groupphase.backend.service.ToolCategoryService;
import at.ac.tuwien.ase.groupphase.backend.service.ToolService;
import at.ac.tuwien.ase.groupphase.backend.service.validation.ToolValidator;
import at.ac.tuwien.ase.groupphase.backend.util.RandomStringGenerator;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.locationtech.jts.geom.Coordinate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

@Service
public class ToolServiceImpl implements ToolService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final ToolRepository toolRepository;
    private final FarmerRepository farmerRepository;
    private final GarageRepository garageRepository;
    private final ToolCategoryService toolCategoryService;
    private final ToolMapper mapper;
    private final ToolValidator validator;

    @Autowired
    public ToolServiceImpl(ToolRepository toolRepository, FarmerRepository farmerRepository, ToolCategoryService toolCategoryService,
                           GarageRepository garageRepository, ToolMapper mapper, ToolValidator validator) {
        this.toolRepository = toolRepository;
        this.farmerRepository = farmerRepository;
        this.toolCategoryService = toolCategoryService;
        this.garageRepository = garageRepository;
        this.mapper = mapper;
        this.validator = validator;
    }

    @Transactional
    public ToolDto createTool(ToolDto dto) throws ValidationException {
        LOGGER.info("creating tool with dto: {}", dto);
        var farmer = getFarmer();
        ToolCategory category = toolCategoryService.getOrCreateToolCategory(dto.getCategoryDto().getName());
        dto.setCategoryDto(new ToolCategoryDto(category.getId(), category.getName()));

        validator.validateForCreate(dto);

        var garage = garageRepository.getGarageByIdAndOwnerId(dto.getGarageDto().getId(), farmer.getId()).orElseThrow(() -> {
            LOGGER.info("Garage with id {} not found for tool creation", dto.getGarageDto().getId());
            return new NotFoundException(String.format("Garage with id %s for logged in user with id %s not found.", dto.getGarageDto().getId(), farmer.getId()));
        });

        final Tool tool = Tool.ToolBuilder.builder()
            .withName(dto.getName())
            .withDescription(dto.getDescription())
            .withCategory(category)
            .withHomeGarage(garage)
            .withCoordinate(new Coordinate(dto.getLocationDto().getLat(), dto.getLocationDto().getLon()))
            .withImage(dto.getImage() == null
                ? null
                : dto.getImage().getBytes(StandardCharsets.UTF_8))
            .withIsUsable(dto.isUsable())
            .withRemainingValue(dto.getRemainingValue())
            .build();

        tool.setOwnerId(farmer.getId());
        tool.setOwner(farmer);

        final Tool returnTool = toolRepository.save(tool);

        return mapper.mapToolEntityToToolDto(returnTool);
    }

    @Override
    @Transactional(readOnly = true)
    public ToolDto getToolById(long id) throws NotFoundException, AccessDeniedException {
        LOGGER.info("getting tool with id: {}", id);
        var farmer = getFarmer();
        Tool tool = toolRepository.findById(id).orElseThrow(() -> {
            LOGGER.info("tool with id {} not found", id);
            return new NotFoundException("Equipment does not exist");
        });

        if (!Objects.equals(tool.getOwner().getId(), farmer.getId())) {
            LOGGER.info("farmer id {} not allowed to view tool owned by {}", farmer.getId(), tool.getOwner().getId());
            throw new AccessDeniedException("Not allowed to view this equipment");
        }

        return mapper.mapToolEntityToToolDto(tool);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ToolDto> getAllTools() {
        LOGGER.info("getting all tools");
        var farmer = getFarmer();
        return toolRepository.findAllByOwner(farmer).stream()
            .map(mapper::mapToolEntityToToolDto)
            .collect(java.util.stream.Collectors.toList());
    }

    @Override
    @Transactional
    public ToolDto updateTool(long id, ToolDto dto) throws ValidationException, NotFoundException, AccessDeniedException {
        LOGGER.info("updating tool with id: {} and dto: {}", id, dto);
        var farmer = getFarmer();
        validator.validateForUpdate(dto);

        Tool existingTool = toolRepository.findById(id).orElseThrow(() -> {
            LOGGER.info("tool with id {} not found for update", id);
            return new NotFoundException("Equipment does not exist");
        });

        if (!Objects.equals(existingTool.getOwner().getId(), farmer.getId())) {
            LOGGER.info("farmer id {} not allowed to update tool owned by {}", farmer.getId(), existingTool.getOwner().getId());
            throw new AccessDeniedException("Not allowed to update this equipment");
        }

        ToolCategory category = toolCategoryService.getOrCreateToolCategory(dto.getCategoryDto().getName());
        var garage = garageRepository.getGarageByIdAndOwnerId(dto.getGarageDto().getId(), farmer.getId()).orElseThrow(() -> {
            LOGGER.info("Garage with id {} not found for tool update", id);
            return new NotFoundException(String.format("Garage with id %s for logged in user with id %s not found.", dto.getGarageDto().getId(), farmer.getId()));
        });

        existingTool.setName(dto.getName());
        existingTool.setDescription(dto.getDescription());
        existingTool.setCategory(category);
        existingTool.setHomeGarage(garage);
        existingTool.setCoordinate(new Coordinate(dto.getLocationDto().getLat(), dto.getLocationDto().getLon()));
        existingTool.setRemainingValue(dto.getRemainingValue());
        existingTool.setUsable(dto.isUsable());
        if (dto.getImage() != null) {
            existingTool.setImage(dto.getImage().getBytes(StandardCharsets.UTF_8));
        }

        Tool saved = toolRepository.save(existingTool);

        return mapper.mapToolEntityToToolDto(saved);
    }


    @Override
    @Transactional
    public void deleteTool(long id) throws NotFoundException, AccessDeniedException {
        LOGGER.info("deleting tool with id: {}", id);
        var farmer = getFarmer();
        Tool tool = toolRepository.findById(id).orElseThrow(() -> {
            LOGGER.info("tool with id {} not found for deletion", id);
            return new NotFoundException("Equipment does not exist");
        });

        if (!Objects.equals(tool.getOwner().getId(), farmer.getId())) {
            LOGGER.info("farmer id {} not allowed to delete tool owned by {}", farmer.getId(), tool.getOwner().getId());
            throw new AccessDeniedException("Not allowed to delete this equipment");
        }

        toolRepository.delete(tool);
    }

    @Override
    public byte[] generateQrCode(CreateQrCodeDto qrCodeDto) throws IOException, WriterException {
        var tool = toolRepository.findByIdAndOwner(qrCodeDto.getId(), getFarmer()).orElseThrow(() -> new NotFoundException(String.format("No tool with the id %s was found.", qrCodeDto.getId())));

        var randomString = RandomStringGenerator.generateRandomString(32);
        tool.setToken(randomString);
        toolRepository.save(tool);

        QRCodeWriter qrCodeWriter = new QRCodeWriter();

        var url = qrCodeDto.getUrl().replace("/token", "/" + randomString).replace("/id/", "/" + tool.getId() + "/");

        BitMatrix bitMatrix = qrCodeWriter.encode(url, BarcodeFormat.QR_CODE, 300, 300);

        ByteArrayOutputStream pngOutput = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutput);
        return pngOutput.toByteArray();
    }

    @Override
    public boolean setNewPosition(long id, String token, CoordinateDto coordinatedto) {
        var toolToUpdate = toolRepository.findById(id).orElseThrow(() ->
            new NotFoundException(String.format("Could not find a tool with the id %s", id)));

        if (!Objects.equals(toolToUpdate.getToken(), token)) {
            throw new InvalidTokenException("The provided token was invalid");
        }

        toolToUpdate.setCoordinate(new Coordinate(coordinatedto.getLat(), coordinatedto.getLon()));

        toolRepository.save((toolToUpdate));

        return true;
    }

    private Farmer getFarmer() throws NotFoundException {
        var activeUser = JwtUtil.getActiveJwtUser();

        return farmerRepository.findById(activeUser.id()).orElseThrow(() -> {
            LOGGER.info("Farmer with id {} not found", activeUser.id());
            return new NotFoundException(String.format("Could not find a farmer with the id %s", activeUser.id()));
        });
    }
}
