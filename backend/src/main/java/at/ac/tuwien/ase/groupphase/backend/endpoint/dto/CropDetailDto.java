package at.ac.tuwien.ase.groupphase.backend.endpoint.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDateTime;

public class CropDetailDto {

    private Long id;

    @NotNull(message = "Crop type must not be null")
    private String cropType;

    @Positive(message = "Current price must be positive")
    private double currentPrice;

    @Positive(message = "Quantity must be positive")
    private double quantity;

    @NotNull(message = "Harvest date must not be null")
    private LocalDateTime harvestDate;

    public CropDetailDto() {
    }

    public CropDetailDto(Long id, String cropType,
                   double currentPrice, double quantity, LocalDateTime harvestDate) {
        this.id = id;
        this.cropType = cropType;
        this.currentPrice = currentPrice;
        this.quantity = quantity;
        this.harvestDate = harvestDate;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCropType() {
        return cropType;
    }

    public void setCropTypeId(String cropType) {
        this.cropType = cropType;
    }

    public double getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(double currentPrice) {
        this.currentPrice = currentPrice;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public LocalDateTime getHarvestDate() {
        return harvestDate;
    }

    public void setHarvestDate(LocalDateTime harvestDate) {
        this.harvestDate = harvestDate;
    }

}
