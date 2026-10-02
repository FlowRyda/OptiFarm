package at.ac.tuwien.ase.groupphase.backend.endpoint;

import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.garage.GarageDto;
import at.ac.tuwien.ase.groupphase.backend.service.GarageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/garages")
public class GarageEndpoint {

    private final GarageService garageService;

    @Autowired
    public GarageEndpoint(GarageService garageService) {
        this.garageService = garageService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<GarageDto> get(@PathVariable("id") Long id) {
        return ResponseEntity.ok(garageService.getGarageOfLoggedInFarmerById(id));
    }

    @GetMapping()
    public ResponseEntity<List<GarageDto>> getAll() {
        return ResponseEntity.ok(garageService.getAllGaragesOfLoggedInFarmerById());
    }

    @PostMapping("/create")
    public ResponseEntity<GarageDto> create(@RequestBody GarageDto garageDto) {
        return ResponseEntity.ok(garageService.createGarage(garageDto));
    }

    @PutMapping("/edit/{id}")
    public ResponseEntity<GarageDto> edit(@PathVariable("id") Long id, @RequestBody GarageDto garageDto) throws Exception {
        return ResponseEntity.ok(garageService.updateGarage(id, garageDto));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        garageService.delete(id);
        return ResponseEntity.ok().build();
    }

}
