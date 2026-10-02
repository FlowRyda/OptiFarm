package at.ac.tuwien.ase.groupphase.backend.endpoint;

import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.LoginDto;
import at.ac.tuwien.ase.groupphase.backend.service.FarmerService;
import jakarta.annotation.security.PermitAll;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.lang.invoke.MethodHandles;

@RestController
@RequestMapping(value = "/api/v1/authentication")
public class LoginEndpoint {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final FarmerService farmerService;

    @Autowired
    public LoginEndpoint(FarmerService farmerService) {
        this.farmerService = farmerService;
    }

    @PermitAll
    @PostMapping
    public String login(@RequestBody LoginDto loginDto) {
        try {
            return farmerService.login(loginDto);
        } catch (Exception e) {
            LOGGER.error("Login failed", e);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
    }
}
