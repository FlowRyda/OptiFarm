package at.ac.tuwien.ase.groupphase.backend.endpoint.dto;

import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.garage.GarageDto;

import java.util.Objects;

public class ToolDto {
    private Long id;
    private String name;
    private String description;
    private double remainingValue;
    private ToolCategoryDto categoryDto;
    private GarageDto garageDto;
    private CoordinateDto locationDto;
    private boolean isUsable;
    // Base64 string
    private String image;

    public ToolDto() {
    }

    public ToolDto(Long id, String name, String description, double remainingValue, ToolCategoryDto category, GarageDto garageDto, CoordinateDto locationDto, boolean isUsable, String image) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.remainingValue = remainingValue;
        this.categoryDto = category;
        this.garageDto = garageDto;
        this.locationDto = locationDto;
        this.isUsable = isUsable;
        this.image = image;
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

    public String getDescription() {
        return description;
    }

    public double getRemainingValue() {
        return remainingValue;
    }

    public void setRemainingValue(double remainingValue) {
        this.remainingValue = remainingValue;
    }

    public ToolCategoryDto getCategoryDto() {
        return categoryDto;
    }

    public GarageDto getGarageDto() {
        return garageDto;
    }

    public void setGarageDto(GarageDto garageDto) {
        this.garageDto = garageDto;
    }

    public CoordinateDto getLocationDto() {
        return locationDto;
    }

    public void setLocationDto(CoordinateDto locationDto) {
        this.locationDto = locationDto;
    }

    public boolean isUsable() {
        return isUsable;
    }

    public String getImage() {
        return image;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setCategoryDto(ToolCategoryDto categoryDto) {
        this.categoryDto = categoryDto;
    }

    public void setUsable(boolean usable) {
        isUsable = usable;
    }

    public void setImage(String image) {
        this.image = image;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ToolDto toolDto = (ToolDto) o;
        return Objects.equals(id, toolDto.id) && Double.compare(remainingValue, toolDto.remainingValue) == 0 && isUsable == toolDto.isUsable
            && Objects.equals(name, toolDto.name) && Objects.equals(description, toolDto.description)
            && Objects.equals(categoryDto, toolDto.categoryDto) && Objects.equals(garageDto, toolDto.garageDto)
            && Objects.equals(locationDto, toolDto.locationDto) && Objects.equals(image, toolDto.image);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, description, remainingValue, categoryDto, isUsable, image);
    }

    @Override
    public String toString() {
        return "ToolDto{" + "id=" + id
            + ", name='" + name + '\''
            + ", description='" + description + '\''
            + ", remainingValue=" + remainingValue
            + ", categoryDto=" + categoryDto
            + ", garageDto=" + garageDto
            + ", locationDto=" + locationDto
            + ", isUsable=" + isUsable
            + ", image='" + image + '\''
            + '}';
    }
}
