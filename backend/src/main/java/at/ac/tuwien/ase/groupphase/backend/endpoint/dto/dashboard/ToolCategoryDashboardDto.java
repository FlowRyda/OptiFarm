package at.ac.tuwien.ase.groupphase.backend.endpoint.dto.dashboard;

import at.ac.tuwien.ase.groupphase.backend.entity.ToolCategory;

public class ToolCategoryDashboardDto {
    private Long id;
    private String name;

    public ToolCategoryDashboardDto() {
    }

    public ToolCategoryDashboardDto(ToolCategory toolCategory) {
        this.id = toolCategory.getId();
        this.name = toolCategory.getName();
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
