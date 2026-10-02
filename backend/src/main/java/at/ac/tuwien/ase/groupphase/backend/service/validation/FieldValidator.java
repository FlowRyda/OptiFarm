package at.ac.tuwien.ase.groupphase.backend.service.validation;

import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.CoordinateDto;
import at.ac.tuwien.ase.groupphase.backend.entity.Farmer;
import at.ac.tuwien.ase.groupphase.backend.entity.Field;
import at.ac.tuwien.ase.groupphase.backend.exception.InvalidFieldException;
import at.ac.tuwien.ase.groupphase.backend.repository.FieldRepository;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.operation.valid.IsValidOp;
import org.locationtech.jts.operation.valid.TopologyValidationError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static at.ac.tuwien.ase.groupphase.backend.entity.Field.calculateAreaInHectares;

@Component
public class FieldValidator {
    private static final Logger LOGGER = LoggerFactory.getLogger(FieldValidator.class);

    private final FieldRepository fieldRepository;
    private final GeometryFactory geometryFactory;

    public FieldValidator(FieldRepository fieldRepository) {
        this.fieldRepository = fieldRepository;
        this.geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    }

    public void validatePolygon(Polygon polygon, Farmer owner, Field excludeField) {
        if (polygon.getNumInteriorRing() > 0) {
            validateExclusionsAreContained(polygon);
            validateExclusionZones(polygon);
        }
        if (!polygon.isValid()) {
            IsValidOp validOp = new IsValidOp(polygon);
            TopologyValidationError error = validOp.getValidationError();


            LOGGER.error("Invalid polygon geometry: {} at coordinate: {}",
                error.getMessage(), error.getCoordinate());


            List<CoordinateDto> intersectionPoint = new ArrayList<>();
            if (error.getCoordinate() != null) {
                CoordinateDto dto = new CoordinateDto();
                dto.setLat(error.getCoordinate().y);
                dto.setLon(error.getCoordinate().x);
                intersectionPoint.add(dto);
            }

            throw new InvalidFieldException(
                "The field boundaries are invalid. Please ensure the boundary lines don't cross each other.", intersectionPoint, null
            );
        }

        double areaInHectares = calculateAreaInHectares(polygon);

        if (areaInHectares < 0.05) {
            LOGGER.warn("Field area too small: {} hectares. Minimum is 0.05 hectares.", areaInHectares);

            throw new InvalidFieldException(
                "Field is too small (" + String.format("%.3f", areaInHectares) + " ha). Minimum size is 0.05 ha."
            );
        }

        if (areaInHectares > 10000) {
            LOGGER.warn("Field area too large: {} hectares. Maximum is 10,000 hectares.", areaInHectares);

            throw new InvalidFieldException(
                "Field is too large (" + String.format("%.2f", areaInHectares) + " ha). Maximum size is 10,000 ha."
            );
        }

        int numPoints = polygon.getNumPoints();
        if (numPoints > 1000) {
            LOGGER.warn("Field has too many boundary points: {}. Maximum is 1000.", numPoints);

            throw new InvalidFieldException(
                "Field boundary is too complex (" + numPoints + " points). Please simplify the shape (maximum 1,000 points)."
            );
        }

        List<Field> existingFields = fieldRepository.findAllByOwner(owner);
        existingFields.remove(excludeField);
        for (Field existingField : existingFields) {
            if (polygon.intersects(existingField.getBoundaries())) {
                Geometry intersection = polygon.intersection(existingField.getBoundaries());

                double intersectionAreaInHectares = Field.calculateAreaInHectares((Polygon) intersection);
                double intersectionArea = intersection.getArea();
                double newFieldArea = polygon.getArea();
                double existingFieldArea = existingField.getBoundaries().getArea();

                double overlapPercentageOfNew = (intersectionArea / newFieldArea) * 100;
                double overlapPercentageOfExisting = (intersectionArea / existingFieldArea) * 100;

                double maxOverlap = Math.max(overlapPercentageOfNew, overlapPercentageOfExisting);

                if (maxOverlap > 0.1 || intersectionAreaInHectares > 0.01) {
                    LOGGER.warn("Field overlap detected - New field: {}% overlap, Existing field '{}': {}% covered, intersection area: {} ha",
                        String.format("%.2f", overlapPercentageOfNew),
                        existingField.getName(),
                        String.format("%.2f", overlapPercentageOfExisting),
                        String.format("%.4f", intersectionAreaInHectares));

                    List<CoordinateDto> overlapCoordinates = convertGeometryToCoordinates(intersection);

                    throw new InvalidFieldException(
                        "Field overlaps with '" + existingField.getName() + "'. Please adjust the boundaries to remove the overlap.",
                        overlapCoordinates,
                        existingField.getName()
                    );
                }

                LOGGER.info("Minor field overlap detected - New field: {}% overlap, Existing field '{}': {}% covered, intersection area: {} ha",
                    String.format("%.2f", overlapPercentageOfNew),
                    existingField.getName(),
                    String.format("%.2f", overlapPercentageOfExisting),
                    String.format("%.4f", intersectionAreaInHectares));
            }
        }
    }


    private void validateExclusionsAreContained(Polygon polygon) {
        LinearRing outerRing = polygon.getExteriorRing();
        Polygon outerBoundary = geometryFactory.createPolygon(outerRing);

        int numHoles = polygon.getNumInteriorRing();

        for (int i = 0; i < numHoles; i++) {
            LinearRing hole = polygon.getInteriorRingN(i);
            Polygon exclusion = geometryFactory.createPolygon(hole);

            if (!outerBoundary.contains(exclusion)) {
                if (outerBoundary.intersects(exclusion)) {
                    Geometry outsidePart = exclusion.difference(outerBoundary);
                    List<CoordinateDto> outsideCoordinates = convertGeometryToCoordinates(outsidePart);

                    throw new InvalidFieldException(
                        "Exclusion zone " + (i + 1) + " extends partially outside the field boundary. Please adjust it to be fully inside.",
                        outsideCoordinates,
                        null
                    );
                } else {
                    List<CoordinateDto> exclusionCoordinates = convertGeometryToCoordinates(exclusion);

                    throw new InvalidFieldException(
                        "Exclusion zone " + (i + 1) + " is completely outside the field boundary. Please move it inside or remove it.",
                        exclusionCoordinates,
                        null
                    );
                }
            }
        }
    }

    private void validateExclusionZones(Polygon polygon) {
        int numHoles = polygon.getNumInteriorRing();

        for (int i = 0; i < numHoles; i++) {
            LinearRing hole = polygon.getInteriorRingN(i);
            Polygon exclusion = geometryFactory.createPolygon(hole);

            if (!exclusion.isValid()) {
                throw new InvalidFieldException("Exclusion zone " + (i + 1) + " crosses itself");
            }

            double area = calculateAreaInHectares(exclusion);
            if (area < 0.001) {
                throw new InvalidFieldException("Exclusion zone " + (i + 1) + " is too small");
            }

            for (int j = i + 1; j < numHoles; j++) {
                LinearRing otherHole = polygon.getInteriorRingN(j);
                Polygon otherExclusion = geometryFactory.createPolygon(otherHole);

                if (exclusion.intersects(otherExclusion)) {
                    Geometry intersection = exclusion.intersection(otherExclusion);
                    List<CoordinateDto> overlapCoordinates = convertGeometryToCoordinates(intersection);

                    throw new InvalidFieldException(
                        "Exclusion zone " + (i + 1) + " overlaps with exclusion zone " + (j + 1),
                        overlapCoordinates,
                        "Exclusion " + (j + 1)
                    );
                }
            }
        }
    }

    private List<CoordinateDto> convertGeometryToCoordinates(Geometry geometry) {
        List<CoordinateDto> coordinates = new ArrayList<>();
        Coordinate[] coords = geometry.getCoordinates();

        for (Coordinate coord : coords) {
            CoordinateDto dto = new CoordinateDto();
            dto.setLat(coord.y);
            dto.setLon(coord.x);
            coordinates.add(dto);
        }

        return coordinates;
    }
}