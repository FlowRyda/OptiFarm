package at.ac.tuwien.ase.groupphase.backend.endpoint.mapper;

import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.CropTypeDto;
import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.dashboard.CropDashboardDto;
import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.dashboard.CropTypeDashboardDto;
import at.ac.tuwien.ase.groupphase.backend.entity.Crop;
import at.ac.tuwien.ase.groupphase.backend.entity.CropType;
import org.springframework.stereotype.Component;

@Component
public class CropTypeMapper {

    public CropTypeDto toDto(CropType entity) {
        if (entity == null) {
            return null;
        }
        CropTypeDto dto = new CropTypeDto();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setGrowthDuration(entity.getGrowthDuration());
        return dto;
    }

    public static CropTypeDashboardDto toDashboardDto(CropType cropType) {
        if (cropType == null) {
            return null;
        }

        return new CropTypeDashboardDto(cropType);
    }


    public CropType toEntity(CropTypeDto dto) {
        if (dto == null) {
            return null;
        }
        CropType entity = new CropType();
        entity.setId(dto.getId());
        entity.setName(dto.getName());
        entity.setGrowthDuration(dto.getGrowthDuration());
        return entity;
    }

    public void updateEntityFromDto(CropTypeDto dto, CropType entity) {
        if (dto == null || entity == null) {
            return;
        }
        entity.setName(dto.getName());
        entity.setGrowthDuration(dto.getGrowthDuration());
    }
}