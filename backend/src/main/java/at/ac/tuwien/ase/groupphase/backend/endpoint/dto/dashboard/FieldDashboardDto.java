package at.ac.tuwien.ase.groupphase.backend.endpoint.dto.dashboard;

import at.ac.tuwien.ase.groupphase.backend.entity.ActiveField;
import at.ac.tuwien.ase.groupphase.backend.entity.Field;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class FieldDashboardDto {

    private Long id;
    private String name;
    private Double area;
    private String seed;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Integer growthStatus;

    public FieldDashboardDto() {
    }

    public FieldDashboardDto(Field field) {
        this.id = field.getId();
        this.name = field.getName();
        this.area = field.getArea();
    }

    public FieldDashboardDto(ActiveField field) {
        this.id = field.getId();
        this.growthStatus = field.getGrowthStatus();
        this.startDate = field.getStartDate();
        this.endDate = field.getEndDate();
        this.seed = field.getSeed().getName();

        var actualField = field.getField();

        this.area = actualField.getArea();
        this.name = actualField.getName();
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

    public Double getArea() {
        return area;
    }

    public void setArea(Double area) {
        this.area = area;
    }

    public String getSeed() {
        return seed;
    }

    public void setSeed(String seed) {
        this.seed = seed;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

    public Integer getGrowthStatus() {
        return growthStatus;
    }

    public void setGrowthStatus(Integer growthStatus) {
        this.growthStatus = growthStatus;
    }
}
