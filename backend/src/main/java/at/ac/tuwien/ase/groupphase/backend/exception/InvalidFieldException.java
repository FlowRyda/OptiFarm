package at.ac.tuwien.ase.groupphase.backend.exception;

import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.CoordinateDto;

import java.util.List;

public class InvalidFieldException extends RuntimeException {
    private List<CoordinateDto> overlappingGeometry;
    private String overlappingFieldName;

    public InvalidFieldException(String message) {
        super(message);
    }

    public InvalidFieldException(String message, List<CoordinateDto> overlappingGeometry, String overlappingFieldName) {
        super(message);
        this.overlappingGeometry = overlappingGeometry;
        this.overlappingFieldName = overlappingFieldName;
    }

    public List<CoordinateDto> getOverlappingGeometry() {
        return overlappingGeometry;
    }

    public String getOverlappingFieldName() {
        return overlappingFieldName;
    }

    public boolean hasOverlapData() {
        return overlappingGeometry != null && !overlappingGeometry.isEmpty();
    }
}