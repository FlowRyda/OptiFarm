package at.ac.tuwien.ase.groupphase.backend.integrationtest;

import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.LoginDto;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class LoginTests extends OptiFarmBaseTest {

    @Test
    public void postLoginReturns200_on_valid_input() throws Exception {
        var result = loginStep.run(loginDto, status().isOk());
        assertNotNull(result);
    }

    @Test
    public void postLoginReturns401_on_invalid_email_input() throws Exception {
        var loginDto = new LoginDto("unknown@doe.com", "password");
        loginStep.run(loginDto, status().isUnauthorized());
    }

    @Test
    public void postLoginReturns401_on_invalid_pw_input() throws Exception {
        var loginDto = new LoginDto("john@doe.com", "passwordwrong");
        loginStep.run(loginDto, status().isUnauthorized());
    }

}
