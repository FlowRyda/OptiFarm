package at.ac.tuwien.ase.groupphase.backend.endpoint.dto;

import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.garage.GarageDto;

import java.util.Objects;

public class VehicleDto extends ToolDto {
    private double fuelLevel;
    private double drivenKm;

    public VehicleDto() {
    }

    public VehicleDto(long id, String name, String description, double remainingValue, ToolCategoryDto category,
                      GarageDto garage, CoordinateDto location, boolean isUsable, String image, double fuelLevel, double drivenKm) {
        super(id, name, description, remainingValue, category, garage, location, isUsable, image);
        this.fuelLevel = fuelLevel;
        this.drivenKm = drivenKm;
    }

    public double getFuelLevel() {
        return fuelLevel;
    }

    public void setFuelLevel(double fuelLevel) {
        this.fuelLevel = fuelLevel;
    }

    public double getDrivenKm() {
        return drivenKm;
    }

    public void setDrivenKm(double drivenKm) {
        this.drivenKm = drivenKm;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        VehicleDto that = (VehicleDto) o;
        return Double.compare(fuelLevel, that.fuelLevel) == 0 && Double.compare(drivenKm, that.drivenKm) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), fuelLevel, drivenKm);
    }

    @Override
    public String toString() {
        return super.toString() + '{'
            + "fuelLevel=" + fuelLevel
            + ", drivenKm=" + drivenKm
            + '}';
    }
}
