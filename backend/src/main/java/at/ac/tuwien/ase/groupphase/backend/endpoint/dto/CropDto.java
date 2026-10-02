package at.ac.tuwien.ase.groupphase.backend.endpoint.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDateTime;

public class CropDto {

    private Long id;

    @NotNull(message = "Farmer ID must not be null")
    private Long farmerId;

    @NotNull(message = "Crop type ID must not be null")
    private Long cropTypeId;

    @Positive(message = "Current price must be positive")
    private double currentPrice;

    @Positive(message = "Quantity must be positive")
    private double quantity;

    @NotNull(message = "Harvest date must not be null")
    private LocalDateTime harvestDate;

    @NotNull(message = "Field ID must not be null")
    private Long fieldId;

    public CropDto() {
    }

    public CropDto(Long id, Long farmerId, Long cropTypeId,
                   double currentPrice, double quantity, LocalDateTime harvestDate, Long fieldId) {
        this.id = id;
        this.farmerId = farmerId;
        this.cropTypeId = cropTypeId;
        this.currentPrice = currentPrice;
        this.quantity = quantity;
        this.harvestDate = harvestDate;
        this.fieldId = fieldId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getFarmerId() {
        return farmerId;
    }

    public void setFarmerId(Long farmerId) {
        this.farmerId = farmerId;
    }

    public Long getCropTypeId() {
        return cropTypeId;
    }

    public void setCropTypeId(Long cropTypeId) {
        this.cropTypeId = cropTypeId;
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

    public Long getFieldId() {
        return fieldId;
    }

    public void setFieldId(Long fieldId) {
        this.fieldId = fieldId;
    }
}
