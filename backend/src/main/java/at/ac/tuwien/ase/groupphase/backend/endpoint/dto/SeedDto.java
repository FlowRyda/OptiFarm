package at.ac.tuwien.ase.groupphase.backend.endpoint.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDateTime;

public class SeedDto {

    private Long id;

    @NotNull(message = "Crop type ID must not be null")
    private Long cropTypeId;

    @Positive(message = "Buying price must be positive")
    private double buyingPrice;

    @NotNull(message = "Buying time must not be null")
    private LocalDateTime buyingTime;

    @NotBlank(message = "Seed name must not be blank")
    private String name;

    public SeedDto() {
    }

    public SeedDto(Long id, Long cropTypeId, double buyingPrice, LocalDateTime buyingTime, String name) {
        this.id = id;
        this.cropTypeId = cropTypeId;
        this.buyingPrice = buyingPrice;
        this.buyingTime = buyingTime;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCropTypeId() {
        return cropTypeId;
    }

    public void setCropTypeId(Long cropTypeId) {
        this.cropTypeId = cropTypeId;
    }

    public double getBuyingPrice() {
        return buyingPrice;
    }

    public void setBuyingPrice(double buyingPrice) {
        this.buyingPrice = buyingPrice;
    }

    public LocalDateTime getBuyingTime() {
        return buyingTime;
    }

    public void setBuyingTime(LocalDateTime buyingTime) {
        this.buyingTime = buyingTime;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
