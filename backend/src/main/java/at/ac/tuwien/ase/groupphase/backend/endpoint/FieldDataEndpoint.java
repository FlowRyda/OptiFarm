package at.ac.tuwien.ase.groupphase.backend.endpoint;

import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.visualization.FieldDataDetailDto;
import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.visualization.FieldDataDto;
import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.visualization.ForecastDto;
import at.ac.tuwien.ase.groupphase.backend.service.FieldDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/fielddata")
public class FieldDataEndpoint {
    private static final Logger logger = LoggerFactory.getLogger(FieldDataEndpoint.class);
    private final FieldDataService fieldDataService;

    @Autowired
    public FieldDataEndpoint(FieldDataService fieldDataService) {
        this.fieldDataService = fieldDataService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<FieldDataDto> getFieldData(@PathVariable("id") Long id) {
        logger.info("Request received: GET /api/v1/fielddata/{}", id);
        FieldDataDto fieldData = fieldDataService.getFieldDataById(id);
        logger.info("Found field data with id {}", id);
        return ResponseEntity.ok(fieldData);
    }

    @GetMapping("/{id}/forecast")
    public ResponseEntity<ForecastDto> getForecast(@PathVariable("id") Long id) {
        logger.info("GET /api/v1/fielddata/{}/forecast", id);
        ForecastDto fieldForecast = fieldDataService.getForecastByFieldId(id);
        logger.info("Found forecast data with id {}", id);
        return ResponseEntity.ok(fieldForecast);
    }

    @GetMapping("/user/{id}")
    public ResponseEntity<List<FieldDataDetailDto>> getFieldDataByOwner(@PathVariable("id") Long id) {
        logger.info("Request received: GET /api/v1/fielddata/user/{}", id);
        List<FieldDataDetailDto> fieldData = fieldDataService.getFieldDataByOwner(id);
        logger.info("Returning {} field data", fieldData.size());
        return ResponseEntity.ok(fieldData);
    }
}
