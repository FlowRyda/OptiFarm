package at.ac.tuwien.ase.groupphase.backend.service;

import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.visualization.FieldDataDetailDto;
import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.visualization.FieldDataDto;
import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.visualization.ForecastDto;

import java.util.List;

public interface FieldDataService {
    /**
     * Retrieves the field data by its id.
     *
     * @param id the id of the field
     * @return the data of the field with the given id
     * @throws RuntimeException if no field is found with the given id
     */
    FieldDataDto getFieldDataById(Long id);

    /**
     * Retrieves the field forecast by field id.
     *
     * @param id the id of the field
     * @return the yield forecast of the field with the given id
     * @throws RuntimeException if no field is found with the given id
     */
    ForecastDto getForecastByFieldId(Long id);

    /**
     * Retrieves all field data for the currently authenticated farmer.
     *
     * @param id the id of the farmer
     * @return a list of field data belonging to the authenticated farmer
     */
    List<FieldDataDetailDto> getFieldDataByOwner(Long id);
}
