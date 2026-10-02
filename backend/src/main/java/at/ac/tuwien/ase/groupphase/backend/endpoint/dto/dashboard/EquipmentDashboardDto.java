package at.ac.tuwien.ase.groupphase.backend.endpoint.dto.dashboard;

import at.ac.tuwien.ase.groupphase.backend.entity.Tool;
import at.ac.tuwien.ase.groupphase.backend.entity.Vehicle;

public class EquipmentDashboardDto {

    private Long id;
    private String name;
    private String description;
    private Double lat;
    private Double lon;
    private double fuelLevel;
    private double drivenKm;
    private boolean isVehicle;

    public EquipmentDashboardDto() {
    }

    public EquipmentDashboardDto(Vehicle vehicle) {
        this.id = vehicle.getId();
        this.name = vehicle.getName();
        this.description = vehicle.getDescription();
        this.lon = vehicle.getCoordinate().x;
        this.lat = vehicle.getCoordinate().y;
        this.drivenKm = vehicle.getDrivenKm();
        this.fuelLevel = vehicle.getFuelLevel();
        this.isVehicle = true;
    }

    public EquipmentDashboardDto(Tool tool) {
        this.id = tool.getId();
        this.name = tool.getName();
        this.description = tool.getDescription();
        this.lat = tool.getCoordinate().x;
        this.lon = tool.getCoordinate().y;
        this.isVehicle = false;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLon() {
        return lon;
    }

    public void setLon(Double lon) {
        this.lon = lon;
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

    public boolean isVehicle() {
        return isVehicle;
    }

    public void setVehicle(boolean vehicle) {
        isVehicle = vehicle;
    }
}
