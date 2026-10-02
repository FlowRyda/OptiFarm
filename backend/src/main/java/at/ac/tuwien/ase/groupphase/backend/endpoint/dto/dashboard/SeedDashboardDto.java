package at.ac.tuwien.ase.groupphase.backend.endpoint.dto.dashboard;

import at.ac.tuwien.ase.groupphase.backend.entity.Seed;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class SeedDashboardDto {

    private Long id;
    private String name;
    private double buyingPrice;
    private LocalDateTime buyingTime;
    private String cropType;

    public SeedDashboardDto() {
    }

    public SeedDashboardDto(Seed seed) {
        this.id = seed.getId();
        this.name = seed.getName();
        this.buyingPrice = seed.getBuyingPrice();
        this.buyingTime = seed.buyingTime();
        this.cropType = seed.getType().getName();
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

    public String getCropType() {
        return cropType;
    }

    public void setCropType(String cropType) {
        this.cropType = cropType;
    }
}
