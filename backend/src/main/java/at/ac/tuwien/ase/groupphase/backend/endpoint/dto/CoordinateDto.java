package at.ac.tuwien.ase.groupphase.backend.endpoint.dto;

import org.locationtech.jts.geom.Coordinate;

public class CoordinateDto {
    private double lat;
    private double lon;

    public CoordinateDto() {
    }

    public CoordinateDto(Coordinate coordinate) {
        this.lat = coordinate.getX();
        this.lon = coordinate.getY();
    }

    public CoordinateDto(double x, double y) {
        this.lat = x;
        this.lon = y;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

}