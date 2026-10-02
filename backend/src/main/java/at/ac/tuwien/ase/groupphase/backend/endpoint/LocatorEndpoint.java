package at.ac.tuwien.ase.groupphase.backend.endpoint;

import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.CoordinateDto;
import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.locator.LocatedObjectDto;
import at.ac.tuwien.ase.groupphase.backend.service.LocatorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/locator")
public class LocatorEndpoint {

    private static final Logger logger = LoggerFactory.getLogger(LocatorEndpoint.class);

    private final LocatorService locatorService;

    @Autowired
    public LocatorEndpoint(LocatorService locatorService) {
        this.locatorService = locatorService;
    }

    @GetMapping("all")
    public ResponseEntity<List<LocatedObjectDto>> getLocatedObjectsFromFarmer() {
        logger.debug("GET /api/v1/locator/all");
        var locatedObjects = locatorService.getLocatedObjectsFromFarmer();
        logger.info("GET /api/v1/locator/all found: {}", locatedObjects.size());
        return ResponseEntity.ok(locatedObjects);
    }
}
