package at.ac.tuwien.ase.groupphase.backend.endpoint.mapper;

import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.dashboard.FieldDashboardDto;
import at.ac.tuwien.ase.groupphase.backend.entity.ActiveField;
import at.ac.tuwien.ase.groupphase.backend.entity.Field;

public class CustomFieldMapper {

    public static FieldDashboardDto toDashboardDto(Field field) {
        if (field == null) {
            return null;
        }

        return new FieldDashboardDto(field);
    }

    public static FieldDashboardDto toDashboardDto(ActiveField activeField) {
        if (activeField == null) {
            return null;
        }

        return new FieldDashboardDto(activeField);
    }
}
