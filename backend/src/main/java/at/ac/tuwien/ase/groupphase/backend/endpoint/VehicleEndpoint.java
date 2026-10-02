package at.ac.tuwien.ase.groupphase.backend.endpoint;

import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.VehicleDto;
import at.ac.tuwien.ase.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.ase.groupphase.backend.service.VehicleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.lang.invoke.MethodHandles;
import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/vehicle")
public class VehicleEndpoint {
    private final VehicleService vehicleService;
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Autowired
    public VehicleEndpoint(VehicleService vehicleService) {
        this.vehicleService = vehicleService;
    }

    @PostMapping
    public ResponseEntity<VehicleDto> createVehicle(@RequestBody VehicleDto dto) throws ValidationException {
        LOGGER.trace("createVehicle() called with dto: {}", dto);
        return ResponseEntity.ok(vehicleService.createVehicle(dto));
    }

    @GetMapping("{id}")
    public ResponseEntity<VehicleDto> getVehicle(@PathVariable("id") long id) {
        LOGGER.trace("getVehicle() called with id: {}", id);
        return ResponseEntity.ok(vehicleService.getVehicleById(id));
    }

    @GetMapping
    public ResponseEntity<List<VehicleDto>> getAllVehicles() {
        LOGGER.trace("getAllVehicles() called");
        return ResponseEntity.ok(vehicleService.getAllVehicles());
    }

    @PutMapping("{id}")
    public ResponseEntity<VehicleDto> updateVehicle(@PathVariable("id") long id, @RequestBody VehicleDto dto) throws ValidationException {
        LOGGER.trace("updateVehicle() called for vehicle {} with dto: {}", id, dto);
        return ResponseEntity.ok(vehicleService.updateVehicle(id, dto));
    }

    @DeleteMapping("{id}")
    public boolean deleteVehicle(@PathVariable("id") long id) {
        LOGGER.trace("deleteVehicle() called for vehicle {}", id);
        vehicleService.deleteVehicle(id);
        return true;
    }

}
