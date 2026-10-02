package at.ac.tuwien.ase.groupphase.backend.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;

import java.util.ArrayList;
import java.util.List;

@Entity
public class CropType extends OwnedObject {
    @Column(nullable = false)

    private double growthDuration;
    @Column(nullable = false)

    private String name;

    @OneToMany(mappedBy = "type", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Seed> seeds = new ArrayList<>();

    public CropType() {
    }

    public CropType(String name, double growthDuration, List<Seed> seeds, Farmer owner) {
        super(owner);
        this.name = name;
        this.growthDuration = growthDuration;
        this.seeds = seeds;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getGrowthDuration() {
        return growthDuration;
    }

    public void setGrowthDuration(double growthDuration) {
        this.growthDuration = growthDuration;
    }
}
