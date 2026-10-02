package at.ac.tuwien.ase.groupphase.backend.endpoint.dto.locator;

import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.CoordinateDto;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Polygon;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


public abstract class LocatedObjectDto {
    private String type;
    private String title;

    @JsonProperty("points")
    private List<CoordinateDto> points;

    @JsonProperty("exclusions")
    private List<List<CoordinateDto>> exclusions;

    public LocatedObjectDto() {
    }

    public LocatedObjectDto(String title, CoordinateDto point) {
        this.title = title;
        this.points = List.of(point);
        this.type = "MARKER";
    }

    public LocatedObjectDto(String title, double lat, double lon) {
        this.title = title;
        this.points = List.of(new CoordinateDto(lat, lon));
        this.type = "MARKER";
    }

    public LocatedObjectDto(String title, Polygon polygon) {
        this.title = title;
        this.type = "POLYGON";

        Coordinate[] coords = polygon.getExteriorRing().getCoordinates();
        this.points = Arrays.stream(coords)
            .map(c -> new CoordinateDto(c.getY(), c.getX()))
            .collect(Collectors.toList());

        this.exclusions = new ArrayList<>();
        for (int i = 0; i < polygon.getNumInteriorRing(); i++) {
            Coordinate[] holeCoords = polygon.getInteriorRingN(i).getCoordinates();
            List<CoordinateDto> hole = Arrays.stream(holeCoords)
                .map(c -> new CoordinateDto(c.getY(), c.getX()))
                .collect(Collectors.toList());
            this.exclusions.add(hole);
        }
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<CoordinateDto> getPoints() {
        return points;
    }

    public void setPoints(List<CoordinateDto> points) {
        this.points = points;
    }

    public List<List<CoordinateDto>> getExclusions() {
        return exclusions;
    }

    public void setExclusions(List<List<CoordinateDto>> exclusions) {
        this.exclusions = exclusions;
    }
}