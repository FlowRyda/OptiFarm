package at.ac.tuwien.ase.groupphase.backend.endpoint.mapper;

import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.CoordinateDto;
import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.field.FieldDetailDto;
import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.field.SimpleFieldDto;
import at.ac.tuwien.ase.groupphase.backend.entity.Field;
import org.locationtech.jts.geom.Polygon;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Mapper
public interface FieldMapper {

    @Mapping(target = "boundaries", source = "boundaries")
    @Mapping(target = "exclusions", expression = "java(mapExclusions(field.getBoundaries()))")
    FieldDetailDto fieldToFieldDetailDto(Field field);

    default List<CoordinateDto> map(Polygon polygon) {
        if (polygon == null) {
            return null;
        }

        return Arrays.stream(polygon.getExteriorRing().getCoordinates())
            .limit(polygon.getExteriorRing().getCoordinates().length - 1)
            .map(coord -> new CoordinateDto(coord.getX(), coord.getY()))
            .collect(Collectors.toList());
    }

    default List<List<CoordinateDto>> mapExclusions(Polygon polygon) {
        if (polygon == null || polygon.getNumInteriorRing() == 0) {
            return new ArrayList<>();
        }

        List<List<CoordinateDto>> exclusions = new ArrayList<>();

        for (int i = 0; i < polygon.getNumInteriorRing(); i++) {
            List<CoordinateDto> hole = Arrays.stream(polygon.getInteriorRingN(i).getCoordinates())
                .limit(polygon.getInteriorRingN(i).getCoordinates().length - 1)
                .map(coord -> new CoordinateDto(coord.getX(), coord.getY()))
                .collect(Collectors.toList());
            exclusions.add(hole);
        }

        return exclusions;
    }

    SimpleFieldDto fieldToSimpleFieldDto(Field field);

    List<SimpleFieldDto> fieldListToSimpleFieldDtoList(List<Field> fieldList);
}