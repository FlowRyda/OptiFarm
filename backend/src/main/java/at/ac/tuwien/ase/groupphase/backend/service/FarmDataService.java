package at.ac.tuwien.ase.groupphase.backend.service;

import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.visualization.FarmDataDto;
import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.visualization.ForecastDto;

public interface FarmDataService {
    /**
     * Retrieves the farm data by the connected user.
     *
     * @param id the id of the farm owner
     * @return the data of the farm with the given owner
     */
    FarmDataDto getFarmDataByOwner(Long id);

    /**
     * Retrieves the farm forecast by the connected user.
     *
     * @param id the id of the farm owner
     * @return the yield forecast of the farm by the connected user
     */
    ForecastDto getFarmForecast(Long id);

}