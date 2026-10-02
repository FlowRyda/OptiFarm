package at.ac.tuwien.ase.groupphase.backend.endpoint.mapper;

import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.activefield.ActiveFieldDetailDto;
import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.activefield.SimpleActiveFieldDto;
import at.ac.tuwien.ase.groupphase.backend.entity.ActiveField;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper
public interface ActiveFieldMapper {
    @Mapping(source = "field.name", target = "name")
    @Mapping(source = "field.id", target = "fieldId")
    @Mapping(source = "startDate", target = "plantedDate")
    @Mapping(source = "seed.name", target = "seedName")
    @Mapping(source = "seed.buyingPrice", target = "seedPrice")
    @Mapping(source = "seed.type.id", target = "cropTypeId")
    @Mapping(source = "owner.id", target = "farmerId")
    @Mapping(source = "expectedHarvestDate", target = "expectedDate")
    @Mapping(source = "sellingPrice", target = "sellingPrice")
    ActiveFieldDetailDto toDetailDto(ActiveField activeField);

    @Mapping(source = "field.name", target = "name")
    @Mapping(source = "field.id", target = "fieldId")
    SimpleActiveFieldDto toSimpleDto(ActiveField activeField);

    @Mapping(source = "field.name", target = "name")
    List<SimpleActiveFieldDto> listToSimpleDtoList(List<ActiveField> activeFieldList);

}
