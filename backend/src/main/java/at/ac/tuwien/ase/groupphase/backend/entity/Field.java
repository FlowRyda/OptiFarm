package at.ac.tuwien.ase.groupphase.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import org.locationtech.jts.geom.Polygon;

import java.util.Objects;

@Entity
public class Field extends OwnedObject {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private double area; //Type might change

    @Column(nullable = false, columnDefinition = "geometry(Polygon, 4326)")
    private Polygon boundaries;

    public Field() {

    }

    public Field(Farmer owner, String name, Polygon boundaries) {
        super(owner);
        this.name = name;
        this.area = calculateAreaInHectares(boundaries);
        this.boundaries = boundaries;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getArea() {
        return area;
    }

    public void setArea(double area) {
        this.area = area;
    }

    public Polygon getBoundaries() {
        return boundaries;
    }

    public void setBoundaries(Polygon boundaries) {
        this.boundaries = boundaries;
        this.area = calculateAreaInHectares(boundaries);
    }


    public static double calculateAreaInHectares(Polygon polygon) {

        double areaInSquareDegrees = polygon.getArea();

        double latitude = polygon.getCentroid().getY();

        double metersPerDegreeLat = 111320.0;
        double metersPerDegreeLon = 111320.0 * Math.cos(Math.toRadians(latitude));

        double areaInSquareMeters = areaInSquareDegrees * metersPerDegreeLat * metersPerDegreeLon;

        return areaInSquareMeters / 10000.0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Field field = (Field) o;
        return Objects.equals(getId(), field.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
}
