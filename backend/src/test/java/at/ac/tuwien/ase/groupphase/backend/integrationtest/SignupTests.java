package at.ac.tuwien.ase.groupphase.backend.integrationtest;

import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.LoginDto;
import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.farmer.CreateFarmerDto;
import at.ac.tuwien.ase.groupphase.backend.integrationtest.steps.LoginStep;
import at.ac.tuwien.ase.groupphase.backend.integrationtest.steps.SignUpStep;
import at.ac.tuwien.ase.groupphase.backend.integrationtest.steps.base.Step;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class SignupTests extends OptiFarmBaseTest {

    @Autowired
    private SignUpStep signUpStep;

    @Test
    public void postSignUpReturns200_on_valid_input() throws Exception {
        var dto = new CreateFarmerDto("First", "Last", "first@last.ed", "password");
        signUpStep.run(dto, status().isOk());
    }

    @Test
    public void postSignUpReturns500_on_invalid_input() throws Exception {
        var dto = new CreateFarmerDto("First", "Last", "invalidMAIL", "password");
        signUpStep.run(dto, status().isInternalServerError());
    }

    @Test
    public void signupThenLoginReturns200_andToken() throws Exception {
        var createFarmer = new CreateFarmerDto("Flow", "User", "flow.user@test.com", "secret123");
        var loginDto = new LoginDto(createFarmer.getEmail(), createFarmer.getPassword());

        Step<CreateFarmerDto, Boolean> signupWithOk =
            dto -> signUpStep.run(dto, status().isOk());

        Step<Boolean, String> loginWithOk =
            ignored -> loginStep.run(loginDto, status().isOk());

        Step<CreateFarmerDto, String> flow = signupWithOk.then(loginWithOk);

        String token = flow.run(createFarmer);

        assertNotNull(token);
        assertFalse(token.isBlank());

    }
}
