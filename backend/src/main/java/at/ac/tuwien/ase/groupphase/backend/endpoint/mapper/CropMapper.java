package at.ac.tuwien.ase.groupphase.backend.endpoint.mapper;

import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.dashboard.CropDashboardDto;
import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.CropDetailDto;
import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.CropDto;
import at.ac.tuwien.ase.groupphase.backend.entity.Crop;
import at.ac.tuwien.ase.groupphase.backend.entity.CropType;
import at.ac.tuwien.ase.groupphase.backend.entity.Farmer;
import at.ac.tuwien.ase.groupphase.backend.entity.Field;

import java.util.List;


public class CropMapper {

    public static CropDto toDto(Crop crop) {
        if (crop == null) {
            return null;
        }
        return new CropDto(
            crop.getId(),
            crop.getOwner() != null ? crop.getOwner().getId() : null,
            crop.getType() != null ? crop.getType().getId() : null,
            crop.getCurrentPrice(),
            crop.getQuantity(),
            crop.getHarvestDate(),
            crop.getFieldId() != null ? crop.getFieldId().getId() : null
        );
    }

    public static CropDetailDto toDetailDto(Crop crop) {
        if (crop == null) {
            return null;
        }
        return new CropDetailDto(
            crop.getId(),
            crop.getType().getName(),
            crop.getCurrentPrice(),
            crop.getQuantity(),
            crop.getHarvestDate()
        );
    }

    public static List<CropDetailDto> toDetailDtoList(List<Crop> crops) {
        if (crops == null) {
            return List.of();
        }

        return crops.stream()
            .map(CropMapper::toDetailDto)
            .toList();
    }

    public static Crop toEntity(CropDto dto, Farmer farmer, CropType type, Field field) {
        if (dto == null) {
            return null;
        }

        Crop crop = new Crop();
        crop.setOwner(farmer);
        crop.setType(type);
        crop.setCurrentPrice(dto.getCurrentPrice());
        crop.setQuantity(dto.getQuantity());
        crop.setHarvestDate(dto.getHarvestDate());
        crop.setFieldId(field);

        return crop;
    }

    public static CropDashboardDto toDashboardDto(Crop crop) {
        if (crop == null) {
            return null;
        }

        return new CropDashboardDto(crop);
    }

}