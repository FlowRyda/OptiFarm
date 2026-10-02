package at.ac.tuwien.ase.groupphase.backend.endpoint.mapper;

import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.dashboard.GarageDashboardDto;
import at.ac.tuwien.ase.groupphase.backend.entity.Garage;


public class GarageMapper {

    public static GarageDashboardDto toDashboardDto(Garage garage) {
        if (garage == null) {
            return null;
        }

        return new GarageDashboardDto(garage);
    }
}
