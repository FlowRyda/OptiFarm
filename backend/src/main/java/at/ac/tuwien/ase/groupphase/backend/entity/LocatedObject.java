package at.ac.tuwien.ase.groupphase.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import org.locationtech.jts.geom.Coordinate;

@MappedSuperclass
public abstract class LocatedObject extends OwnedObject {

    @Column
    private Coordinate coordinate;

    public LocatedObject() {

    }

    public LocatedObject(Coordinate coordinate, Farmer farmer) {
        super(farmer);
        this.coordinate = coordinate;
    }

    public Coordinate getCoordinate() {
        return coordinate;
    }

    public void setCoordinate(Coordinate coordinate) {
        this.coordinate = coordinate;
    }

}
