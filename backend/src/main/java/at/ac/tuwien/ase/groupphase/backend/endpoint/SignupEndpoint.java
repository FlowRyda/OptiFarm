package at.ac.tuwien.ase.groupphase.backend.endpoint;

import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.farmer.CreateFarmerDto;
import at.ac.tuwien.ase.groupphase.backend.service.FarmerService;
import jakarta.annotation.security.PermitAll;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.invoke.MethodHandles;

@RestController
@RequestMapping(value = "/api/v1/signup")
public class SignupEndpoint {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final FarmerService farmerService;

    @Autowired
    public SignupEndpoint(FarmerService farmerService) {
        this.farmerService = farmerService;
    }

    @PermitAll
    @PostMapping
    public ResponseEntity<Boolean> signup(@RequestBody CreateFarmerDto createFarmerDto) {
        LOGGER.trace("signup called with {}", createFarmerDto.redactedToString());
        farmerService.create(createFarmerDto);
        return ResponseEntity.ok(true);
    }
}
