package at.ac.tuwien.ase.groupphase.backend.endpoint;

import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.visualization.FarmDataDto;
import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.visualization.ForecastDto;
import at.ac.tuwien.ase.groupphase.backend.service.FarmDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/farmdata")
public class FarmDataEndpoint {
    private static final Logger logger = LoggerFactory.getLogger(FarmDataEndpoint.class);
    private final FarmDataService farmDataService;

    @Autowired
    public FarmDataEndpoint(FarmDataService farmDataService) {
        this.farmDataService = farmDataService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<FarmDataDto> getFarmDataByOwner(@PathVariable("id") Long id) {
        logger.info("Request received: GET /api/v1/farmdata/{}", id);
        FarmDataDto farmData = farmDataService.getFarmDataByOwner(id);
        logger.info("Found farm data with id {}", id);
        return ResponseEntity.ok(farmData);
    }

    @GetMapping("/{id}/forecast")
    public ResponseEntity<ForecastDto> getForecast(@PathVariable("id") Long id) {
        logger.info("GET /api/v1/farmdata/{}/forecast", id);
        ForecastDto farmForecast = farmDataService.getFarmForecast(id);
        logger.info("Found forecast data with id {}", id);
        return ResponseEntity.ok(farmForecast);
    }
}
