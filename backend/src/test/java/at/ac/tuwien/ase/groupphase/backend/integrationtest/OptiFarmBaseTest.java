package at.ac.tuwien.ase.groupphase.backend.integrationtest;

import at.ac.tuwien.ase.groupphase.backend.configuration.PostgresConfig;
import at.ac.tuwien.ase.groupphase.backend.configuration.TestFlowDataSection;
import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.LoginDto;
import at.ac.tuwien.ase.groupphase.backend.integrationtest.steps.LoginStep;
import at.ac.tuwien.ase.groupphase.backend.integrationtest.steps.base.Step;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@Import(PostgresConfig.class)
@Transactional
@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class OptiFarmBaseTest {
    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected LoginStep loginStep;

    protected LoginDto loginDto = new LoginDto("john@doe.com", "password");

    private TestFlowDataSection testFlowDataSection;

    @BeforeEach
    void setUpFlowSection() {
        testFlowDataSection = TestFlowDataSection.activate();
    }

    @AfterEach
    void tearDownFlowSection() {
        if (testFlowDataSection != null) {
            testFlowDataSection.close();
            testFlowDataSection = null;
        }
    }

    protected Step<LoginDto, String> loginWithOk() {
        return dto -> loginStep.run(dto, status().isOk());
    }
}
