package at.ac.tuwien.ase.groupphase.backend.endpoint.mapper;

import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.CoordinateDto;
import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.ToolCategoryDto;
import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.ToolDto;
import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.garage.GarageDto;
import at.ac.tuwien.ase.groupphase.backend.entity.Tool;
import at.ac.tuwien.ase.groupphase.backend.entity.ToolCategory;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
public class ToolMapper {
    public ToolDto mapToolEntityToToolDto(Tool tool) {
        ToolDto dto = new ToolDto();
        var coordinate = tool.getHomeGarage().getCoordinate();
        var garage = new GarageDto(tool); //no tools loaded to avoid recursion
        garage.setCoordinate(new CoordinateDto(coordinate.getX(), coordinate.getY()));

        dto.setId(tool.getId());
        dto.setName(tool.getName());
        dto.setDescription(tool.getDescription());
        dto.setCategoryDto(new ToolCategoryDto(tool.getCategory().getId(), tool.getCategory().getName()));
        dto.setGarageDto(garage);
        dto.setLocationDto(new CoordinateDto(tool.getCoordinate().getX(), tool.getCoordinate().getY()));
        dto.setRemainingValue(tool.getRemainingValue());
        dto.setUsable(tool.isUsable());
        if (tool.getImage() != null) {
            dto.setImage(new String(tool.getImage(), StandardCharsets.UTF_8));
        }

        return dto;
    }

    public ToolCategoryDto mapToolCategoryEntityToToolCategoryDto(ToolCategory toolCategory) {
        ToolCategoryDto dto = new ToolCategoryDto();
        dto.setId(toolCategory.getId());
        dto.setName(toolCategory.getName());
        return dto;
    }

    public List<ToolCategoryDto> mapToolCategoryEntitiesToToolCategoryDtoList(List<ToolCategory> toolCategories) {
        return toolCategories.stream()
            .map(this::mapToolCategoryEntityToToolCategoryDto)
            .toList();
    }
}
