package at.ac.tuwien.ase.groupphase.backend.entity;

import jakarta.persistence.Entity;
import org.locationtech.jts.geom.Coordinate;

@Entity
public class Vehicle extends Tool {
    private double fuelLevel;
    private double drivenKm;

    public Vehicle() {
    }

    public Vehicle(String name, String description, double remainingValue, ToolCategory category,
                   Garage homeGarage, Coordinate coordinate, boolean isUsable, byte[] image, double fuelLevel, double drivenKm) {
        super(name, category, description, remainingValue, homeGarage, coordinate, isUsable, image);
        this.fuelLevel = fuelLevel;
        this.drivenKm = drivenKm;
    }

    public Vehicle(String name, ToolCategory category, String description, double remainingValue, byte[] image, Garage garage, Farmer farmer, Coordinate coordinate, boolean isUsable, double fuelLevel, double drivenKm) {
        super(name, category, description, remainingValue, image, garage, farmer, coordinate, isUsable);
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
}
