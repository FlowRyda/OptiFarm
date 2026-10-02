package at.ac.tuwien.ase.groupphase.backend.endpoint;

import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.farmer.ChangePasswordDto;
import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.farmer.EditFarmerDto;
import at.ac.tuwien.ase.groupphase.backend.service.FarmerService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.invoke.MethodHandles;

@RestController
@RequestMapping(value = "/api/v1/farmers")
public class FarmerEndpoint {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final FarmerService farmerService;

    @Autowired
    public FarmerEndpoint(FarmerService farmerService) {
        this.farmerService = farmerService;
    }

    @GetMapping
    public ResponseEntity<EditFarmerDto> getLoggedInFarmer() {
        LOGGER.trace("Get logged in farmer");
        var farmer = farmerService.getLoggedInFarmer();
        return ResponseEntity.ok(farmer);
    }

    @PutMapping
    public ResponseEntity<EditFarmerDto> updateLoggedInFarmer(@Valid @RequestBody EditFarmerDto farmerDto) {
        LOGGER.trace("Update logged in farmer {}", farmerDto);
        var changedFarmer = farmerService.update(farmerDto);
        return ResponseEntity.ok(changedFarmer);
    }

    @PutMapping("/changePassword")
    public ResponseEntity<Boolean> changePasswordOfLoggedInFarmer(@Valid @RequestBody ChangePasswordDto changePasswordDto) {
        LOGGER.trace("Change password of logged in farmer");
        farmerService.changePassword(changePasswordDto);
        return ResponseEntity.ok(true);
    }

    @DeleteMapping
    public boolean deleteLoggedInFarmer() {
        LOGGER.trace("Delete logged in farmer");
        farmerService.delete();
        return true;
    }

}
