package at.ac.tuwien.ase.groupphase.backend.endpoint.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public class CropTypeDto {
    private Long id;
    @NotBlank(message = "Name must not be empty")
    private String name;
    @Positive(message = "Growth duration must be a positive number")
    private double growthDuration;

    public CropTypeDto() {
    }

    public CropTypeDto(Long id, String name, double growthDuration) {
        this.id = id;
        this.name = name;
        this.growthDuration = growthDuration;
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

    public double getGrowthDuration() {
        return growthDuration;
    }

    public void setGrowthDuration(double growthDuration) {
        this.growthDuration = growthDuration;
    }
}