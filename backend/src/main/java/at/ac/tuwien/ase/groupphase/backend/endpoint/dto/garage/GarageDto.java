package at.ac.tuwien.ase.groupphase.backend.endpoint.dto.garage;

import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.CoordinateDto;
import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.ToolDto;
import at.ac.tuwien.ase.groupphase.backend.endpoint.mapper.ToolMapper;
import at.ac.tuwien.ase.groupphase.backend.entity.Garage;
import at.ac.tuwien.ase.groupphase.backend.entity.Tool;
import org.locationtech.jts.geom.Coordinate;

import java.util.List;

public class GarageDto {
    private Long id;
    private Long ownerId;
    private String name;

    private CoordinateDto coordinate;

    private List<ToolDto> tools;

    public GarageDto() {
    }

    public GarageDto(Long id, Long ownerId, String name, CoordinateDto coordinate, ToolDto... tools) {
        this.id = id;
        this.ownerId = ownerId;
        this.name = name;
        this.coordinate = coordinate;
        this.tools = List.of(tools);
    }

    public GarageDto(Tool tool) {
        var garage = tool.getHomeGarage();
        this.id = garage.getId();
        this.ownerId = garage.getOwnerId();
        this.name = garage.getName();
        this.coordinate = new CoordinateDto(garage.getCoordinate());
    }


    public GarageDto(Garage garage) {
        this.id = garage.getId();
        this.ownerId = garage.getOwnerId();
        this.name = garage.getName();
        this.coordinate = new CoordinateDto(garage.getCoordinate());
    }

    public void loadTools(Garage garage) {
        var toolMapper = new ToolMapper();
        this.tools = garage.getTools().stream().map(toolMapper::mapToolEntityToToolDto).toList();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    public CoordinateDto getCoordinate() {
        return coordinate;
    }

    public void setCoordinate(CoordinateDto coordinate) {
        this.coordinate = coordinate;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<ToolDto> getTools() {
        return tools;
    }

    public void setTools(List<ToolDto> tools) {
        this.tools = tools;
    }
}
