package at.ac.tuwien.ase.groupphase.backend.datagenerator;

import at.ac.tuwien.ase.groupphase.backend.entity.Farmer;
import at.ac.tuwien.ase.groupphase.backend.repository.FarmerRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Profile("test")
@Component
public class FarmerTestDataGenerator {

    private final FarmerRepository farmerRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public FarmerTestDataGenerator(FarmerRepository farmerRepository, PasswordEncoder passwordEncoder) {
        this.farmerRepository = farmerRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostConstruct
    private void generateFarmer() {
        if (farmerRepository.findAll().isEmpty()) {
            var hashedPw = passwordEncoder.encode("password");
            Farmer farmer = Farmer.FarmerBuilder.aFarmer()
                .withFirstname("John")
                .withLastname("Doe")
                .withEmail("john@doe.com")
                .withHashedPassword(hashedPw)
                .build();
            farmerRepository.save(farmer);

        }
    }
}
