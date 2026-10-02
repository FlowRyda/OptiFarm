package at.ac.tuwien.ase.groupphase.backend.endpoint.mapper;

import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.SeedDto;
import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.dashboard.SeedDashboardDto;
import at.ac.tuwien.ase.groupphase.backend.entity.CropType;
import at.ac.tuwien.ase.groupphase.backend.entity.Seed;
import org.springframework.stereotype.Component;

@Component
public class SeedMapper {

    public static SeedDto toDto(Seed seed) {
        if (seed == null) {
            return null;
        }
        return new SeedDto(
            seed.getId(),
            seed.getType() != null ? seed.getType().getId() : null,
            seed.getBuyingPrice(),
            seed.buyingTime(),
            seed.getName()
        );
    }

    public static SeedDashboardDto toDashboardDto(Seed seed) {
        if (seed == null) {
            return null;
        }

        return new SeedDashboardDto(seed);
    }

    public static Seed toEntity(SeedDto dto, CropType cropType) {
        if (dto == null) {
            return null;
        }

        Seed seed = new Seed()

            .setType(cropType)
            .setBuyingPrice(dto.getBuyingPrice())
            .setName(dto.getName())
            .setBuyingTime(dto.getBuyingTime());
        return seed;
    }

    public static void updateEntityFromDto(SeedDto dto, Seed entity, CropType cropType) {
        entity.setName(dto.getName());
        entity.setBuyingPrice(dto.getBuyingPrice());
        entity.setBuyingTime(dto.getBuyingTime());
        if (cropType != null) {
            entity.setType(cropType);
        }
    }
}
