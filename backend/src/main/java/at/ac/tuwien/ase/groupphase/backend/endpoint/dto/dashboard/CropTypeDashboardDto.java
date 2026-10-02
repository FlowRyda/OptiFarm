package at.ac.tuwien.ase.groupphase.backend.endpoint.dto.dashboard;

import at.ac.tuwien.ase.groupphase.backend.entity.CropType;

public class CropTypeDashboardDto {

    private Long id;
    private String name;

    public CropTypeDashboardDto() {
    }

    public CropTypeDashboardDto(CropType cropType) {
        this.id = cropType.getId();
        this.name = cropType.getName();
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
}
