package at.ac.tuwien.ase.groupphase.backend.endpoint.mapper;

import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.dashboard.EquipmentDashboardDto;
import at.ac.tuwien.ase.groupphase.backend.entity.Tool;
import at.ac.tuwien.ase.groupphase.backend.entity.Vehicle;

public class EquipmentMapper {

    public static EquipmentDashboardDto toDashboardDto(Tool tool) {
        if (tool == null) {
            return null;
        }

        if (tool instanceof Vehicle vehicle) {
            return new EquipmentDashboardDto(vehicle);
        }

        return new EquipmentDashboardDto(tool);
    }
}
