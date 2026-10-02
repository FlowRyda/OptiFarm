package at.ac.tuwien.ase.groupphase.backend.endpoint.mapper;

import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.CoordinateDto;
import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.ToolCategoryDto;
import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.VehicleDto;
import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.garage.GarageDto;
import at.ac.tuwien.ase.groupphase.backend.entity.Vehicle;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
public class VehicleMapper {
    public VehicleDto mapVehicleEntityToDto(Vehicle vehicle) {
        VehicleDto dto = new VehicleDto();
        var coordinate = vehicle.getHomeGarage().getCoordinate();
        var garage = new GarageDto(vehicle.getHomeGarage());
        garage.setCoordinate(new CoordinateDto(coordinate.getX(), coordinate.getY()));

        dto.setId(vehicle.getId());
        dto.setName(vehicle.getName());
        dto.setDescription(vehicle.getDescription());
        dto.setCategoryDto(new ToolCategoryDto(vehicle.getCategory().getId(), vehicle.getCategory().getName()));
        dto.setGarageDto(garage);
        dto.setLocationDto(new CoordinateDto(vehicle.getCoordinate().getX(), vehicle.getCoordinate().getY()));
        dto.setRemainingValue(vehicle.getRemainingValue());
        dto.setUsable(vehicle.isUsable());
        if (vehicle.getImage() != null) {
            dto.setImage(new String(vehicle.getImage(), StandardCharsets.UTF_8));
        }
        dto.setDrivenKm(vehicle.getDrivenKm());
        dto.setFuelLevel(vehicle.getFuelLevel());

        return dto;
    }
}
