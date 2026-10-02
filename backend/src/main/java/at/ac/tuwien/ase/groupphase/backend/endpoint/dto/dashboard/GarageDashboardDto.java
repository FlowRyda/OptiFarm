package at.ac.tuwien.ase.groupphase.backend.endpoint.dto.dashboard;

import at.ac.tuwien.ase.groupphase.backend.entity.Garage;
import at.ac.tuwien.ase.groupphase.backend.entity.Seed;

public class GarageDashboardDto {

    private Long id;
    private String name;
    private Integer toolCount;
    private Double lat;
    private Double lon;

    public GarageDashboardDto() {
    }

    public GarageDashboardDto(Garage garage) {
        this.id = garage.getId();
        this.name = garage.getName();
        this.toolCount = garage.getTools().size();
        this.lat = garage.getCoordinate().getX();
        this.lon = garage.getCoordinate().getY();
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

    public Integer getToolCount() {
        return toolCount;
    }

    public void setToolCount(Integer toolCount) {
        this.toolCount = toolCount;
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
}
