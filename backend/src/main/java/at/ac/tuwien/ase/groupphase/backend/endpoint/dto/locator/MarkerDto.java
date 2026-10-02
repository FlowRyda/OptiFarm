package at.ac.tuwien.ase.groupphase.backend.endpoint.dto.locator;

import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.CoordinateDto;
import at.ac.tuwien.ase.groupphase.backend.entity.Garage;
import at.ac.tuwien.ase.groupphase.backend.entity.Tool;
import at.ac.tuwien.ase.groupphase.backend.entity.Vehicle;


public class MarkerDto extends LocatedObjectDto {

    private double fuelLevel;
    private double markerId;
    private boolean isUsable;
    private String iconUrl;
    private int iconWidth = 35;
    private int iconHeight = 55;
    private boolean isGarage;

    public MarkerDto() {
    }

    public MarkerDto(Tool tool) {
        super(tool.getName(), tool.getCoordinate().getX(), tool.getCoordinate().getY());
        this.markerId = tool.getId();
        this.isUsable = tool.isUsable();
        this.iconUrl = "assets/marker/tool.svg";
        this.fuelLevel = -1;
        this.isGarage = false;
    }

    public MarkerDto(Vehicle vehicle) {
        super(vehicle.getName(), vehicle.getCoordinate().getX(), vehicle.getCoordinate().getY());
        this.markerId = vehicle.getId();
        this.isUsable = vehicle.isUsable();
        this.fuelLevel = vehicle.getFuelLevel();
        this.isGarage = false;

        if (fuelLevel < 25) {
            this.iconUrl = "assets/marker/low-fuel-tractor.svg";
        } else {
            this.iconUrl = "assets/marker/tractor.svg";
        }
    }

    public MarkerDto(Garage garage) {
        super(garage.getName(), garage.getCoordinate().getX(), garage.getCoordinate().getY());
        this.markerId = garage.getId();
        this.isUsable = true;
        this.fuelLevel = -1;
        this.iconUrl = "assets/marker/garage.svg";
        this.isGarage = true;
    }

    public MarkerDto(double latitude, double longitude, String title) {
        super(title, new CoordinateDto(latitude, longitude));
    }

    public MarkerDto(double latitude, double longitude, String title, String iconUrl, int iconWidth, int iconHeight) {
        super(title, new CoordinateDto(latitude, longitude));

        this.iconUrl = iconUrl;
        this.iconWidth = iconWidth;
        this.iconHeight = iconHeight;
    }


    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public int getIconWidth() {
        return iconWidth;
    }

    public void setIconWidth(int iconWidth) {
        this.iconWidth = iconWidth;
    }

    public int getIconHeight() {
        return iconHeight;
    }

    public void setIconHeight(int iconHeight) {
        this.iconHeight = iconHeight;
    }

    public boolean isUsable() {
        return isUsable;
    }

    public void setUsable(boolean isUsable) {
        this.isUsable = isUsable;
    }

    public double getFuelLevel() {
        return fuelLevel;
    }

    public void setFuelLevel(double fuelLevel) {
        this.fuelLevel = fuelLevel;
    }

    public double getMarkerId() {
        return markerId;
    }

    public void setMarkerId(double markerId) {
        this.markerId = markerId;
    }

    public boolean isGarage() {
        return isGarage;
    }

    public void setGarage(boolean garage) {
        isGarage = garage;
    }
}
