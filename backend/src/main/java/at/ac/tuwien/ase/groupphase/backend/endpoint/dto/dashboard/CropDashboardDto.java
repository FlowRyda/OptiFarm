package at.ac.tuwien.ase.groupphase.backend.endpoint.dto.dashboard;

import at.ac.tuwien.ase.groupphase.backend.entity.Crop;

import java.time.LocalDateTime;

public class CropDashboardDto {

    private Long id;
    private String name;
    private double currentPrice;
    private Double quantity;
    private LocalDateTime harvestDate;

    public CropDashboardDto() {
    }

    public CropDashboardDto(Crop crop) {
        this.id = crop.getId();
        this.name = crop.getType().getName();
        this.currentPrice = crop.getCurrentPrice();
        this.quantity = crop.getQuantity();
        this.harvestDate = crop.getHarvestDate();
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

    public double getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(double currentPrice) {
        this.currentPrice = currentPrice;
    }

    public Double getQuantity() {
        return quantity;
    }

    public void setQuantity(Double quantity) {
        this.quantity = quantity;
    }

    public LocalDateTime getHarvestDate() {
        return harvestDate;
    }

    public void setHarvestDate(LocalDateTime harvestDate) {
        this.harvestDate = harvestDate;
    }
}
