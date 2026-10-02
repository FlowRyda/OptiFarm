package at.ac.tuwien.ase.groupphase.backend.entity;


import jakarta.persistence.*;
import org.locationtech.jts.geom.Coordinate;

import java.util.List;

@Entity
public class Garage extends LocatedObject {
    @Column(nullable = false)
    private String name;

    @OneToMany(mappedBy = "homeGarage")
    private List<Tool> tools;

    public Garage() {
    }

    public Garage(String name, Coordinate coordinate, Farmer farmer, List<Tool> tools) {
        super(coordinate, farmer);
        this.name = name;
        this.tools = tools;
    }

    public List<Tool> getTools() {
        return tools;
    }

    public void setTools(List<Tool> tools) {
        this.tools = tools;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
