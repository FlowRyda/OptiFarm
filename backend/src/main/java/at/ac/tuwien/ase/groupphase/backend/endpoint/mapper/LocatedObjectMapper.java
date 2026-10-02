package at.ac.tuwien.ase.groupphase.backend.endpoint.mapper;

import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.locator.MarkerDto;
import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.locator.PolygonDto;
import at.ac.tuwien.ase.groupphase.backend.entity.*;

public class LocatedObjectMapper {

    public static MarkerDto toDto(Tool tool) {
        if (tool == null) {
            return null;
        }

        if (tool instanceof Vehicle) {
            return new MarkerDto((Vehicle) tool);
        }

        return new MarkerDto(tool);
    }

    public static MarkerDto toDto(Garage garage) {
        if (garage == null) {
            return null;
        }

        return new MarkerDto(garage);
    }

    public static MarkerDto toDto(Vehicle vehicle) {
        if (vehicle == null) {
            return null;
        }

        return new MarkerDto(vehicle);
    }

    public static PolygonDto toDto(ActiveField field) {
        return new PolygonDto(field);
    }

    public static PolygonDto toDto(Field field) {
        return new PolygonDto(field);
    }
}
