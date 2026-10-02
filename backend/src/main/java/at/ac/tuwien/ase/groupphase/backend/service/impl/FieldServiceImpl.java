package at.ac.tuwien.ase.groupphase.backend.service.impl;

import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.CoordinateDto;
import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.field.FieldCreateDto;
import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.field.FieldDetailDto;
import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.field.FieldUpdateDto;
import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.field.FieldValidationRequestDto;
import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.field.FieldValidationResponseDto;
import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.field.SimpleFieldDto;
import at.ac.tuwien.ase.groupphase.backend.endpoint.mapper.FieldMapper;
import at.ac.tuwien.ase.groupphase.backend.entity.Farmer;
import at.ac.tuwien.ase.groupphase.backend.entity.Field;
import at.ac.tuwien.ase.groupphase.backend.exception.AccessDeniedException;
import at.ac.tuwien.ase.groupphase.backend.exception.InvalidFieldException;
import at.ac.tuwien.ase.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.ase.groupphase.backend.repository.CropRepository;
import at.ac.tuwien.ase.groupphase.backend.repository.FarmerRepository;
import at.ac.tuwien.ase.groupphase.backend.repository.FieldRepository;
import at.ac.tuwien.ase.groupphase.backend.security.JwtUtil;
import at.ac.tuwien.ase.groupphase.backend.service.FieldService;
import at.ac.tuwien.ase.groupphase.backend.service.validation.FieldValidator;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.PrecisionModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FieldServiceImpl implements FieldService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final int SRID = 4326; // WGS84 for GPS coordinates

    private final FieldRepository fieldRepository;
    private final FieldMapper fieldMapper;
    private final FarmerRepository farmerRepository;
    private final CropRepository cropRepository;
    private final FieldValidator fieldValidator;

    public FieldServiceImpl(FieldRepository fieldRepository, FieldMapper fieldMapper, FarmerRepository farmerRepository,
                            CropRepository cropRepository, FieldValidator fieldValidator) {
        this.fieldRepository = fieldRepository;
        this.fieldMapper = fieldMapper;
        this.farmerRepository = farmerRepository;
        this.cropRepository = cropRepository;
        this.fieldValidator = fieldValidator;
    }

    @Override
    @Transactional
    public SimpleFieldDto create(FieldCreateDto fieldDto) {
        LOGGER.info("Saving new field {}", fieldDto.name());

        Farmer owner = getFarmer();

        Polygon polygon = convertToPolygon(fieldDto.boundaries(), fieldDto.exclusions());
        fieldValidator.validatePolygon(polygon, owner, null);
        Field field = new Field(owner, fieldDto.name(), polygon);

        LOGGER.info("Calculated field area: {} hectares", field.getArea());

        Field savedField = fieldRepository.save(field);
        LOGGER.info("Saved field with id {}", savedField.getId());

        return fieldMapper.fieldToSimpleFieldDto(savedField);
    }


    private Polygon convertToPolygon(List<CoordinateDto> boundaries, List<List<CoordinateDto>> exclusions) {
        GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), SRID);

        List<Coordinate> outerCoords = boundaries.stream()
            .map(c -> new Coordinate(c.getLon(), c.getLat()))
            .collect(Collectors.toCollection(ArrayList::new));

        if (!outerCoords.get(0).equals2D(outerCoords.get(outerCoords.size() - 1))) {
            outerCoords.add(new Coordinate(outerCoords.get(0)));
        }

        LinearRing shell = geometryFactory.createLinearRing(outerCoords.toArray(new Coordinate[0]));

        LinearRing[] holes = null;
        if (exclusions != null && !exclusions.isEmpty()) {
            holes = exclusions.stream()
                .map(exclusion -> {
                    List<Coordinate> holeCoords = exclusion.stream()
                        .map(c -> new Coordinate(c.getLon(), c.getLat()))
                        .collect(Collectors.toCollection(ArrayList::new));

                    if (!holeCoords.get(0).equals2D(holeCoords.get(holeCoords.size() - 1))) {
                        holeCoords.add(new Coordinate(holeCoords.get(0)));
                    }

                    return geometryFactory.createLinearRing(holeCoords.toArray(new Coordinate[0]));
                })
                .toArray(LinearRing[]::new);
        }

        return geometryFactory.createPolygon(shell, holes);
    }

    @Override
    @Transactional
    public SimpleFieldDto update(FieldUpdateDto fieldDto, Long id) {
        LOGGER.info("Updating field with id {}: {}", id, fieldDto.name());
        Field existingField = fieldRepository.findById(id).orElseThrow(() -> new NotFoundException("Field not found"));

        if (!existingField.getOwner().equals(getFarmer())) {
            throw new AccessDeniedException("You are not allowed to edit this field");
        }
        existingField.setName(fieldDto.name());
        Polygon polygon = convertToPolygon(fieldDto.boundaries(), fieldDto.exclusions());
        fieldValidator.validatePolygon(polygon, getFarmer(), existingField);
        existingField.setBoundaries(polygon);

        LOGGER.info("Calculated field area: {} hectares", existingField.getArea());

        Field savedField = fieldRepository.save(existingField);
        LOGGER.info("Updated field with id {}", savedField.getId());
        return fieldMapper.fieldToSimpleFieldDto(savedField);
    }

    @Override
    @Transactional
    public FieldDetailDto getFieldById(Long id) {
        LOGGER.info("Fetching field with id {}", id);
        Field existingField = fieldRepository.findById(id).orElseThrow(() -> new NotFoundException("Field not found"));
        if (!existingField.getOwner().equals(getFarmer())) {
            throw new AccessDeniedException("You are not allowed to edit this field");
        }
        return fieldMapper.fieldToFieldDetailDto(existingField);
    }

    @Override
    @Transactional
    public void deleteFieldById(Long id) {
        LOGGER.info("Deleting field with id {}", id);
        Field existingField = fieldRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Field not found"));

        if (!existingField.getOwner().equals(getFarmer())) {
            throw new AccessDeniedException("You are not allowed to delete this field");
        }

        cropRepository.deleteAllByField(existingField);
        fieldRepository.delete(existingField);
        LOGGER.info("Deleted field with id {}", id);
    }

    @Override
    @Transactional
    public List<SimpleFieldDto> getAllFields() {
        LOGGER.info("Fetching all fields");

        Farmer owner = getFarmer();
        List<Field> fields = fieldRepository.findAllByOwner(owner);
        LOGGER.info("Fetched {} fields", fields.size());
        return fieldMapper.fieldListToSimpleFieldDtoList(fields);
    }

    @Override
    @Transactional(readOnly = true)
    public FieldValidationResponseDto validateField(FieldValidationRequestDto request) {
        try {
            Polygon polygon = convertToPolygon(request.boundaries(), request.exclusions());

            Field excludeField = null;
            if (request.excludeFieldId() != null) {
                excludeField = fieldRepository.findById(request.excludeFieldId()).orElse(null);
            }

            fieldValidator.validatePolygon(polygon, getFarmer(), excludeField);
            return new FieldValidationResponseDto(true, null, null, null);
        } catch (InvalidFieldException e) {
            return new FieldValidationResponseDto(false, e.getMessage(), e.getOverlappingGeometry(), e.getOverlappingFieldName());
        }
    }

    private Farmer getFarmer() throws NotFoundException {
        var activeUser = JwtUtil.getActiveJwtUser();

        return farmerRepository.findById(activeUser.id()).orElseThrow(() ->
            new NotFoundException(String.format("Could not find a farmer with the id %s", activeUser.id())));
    }
}
