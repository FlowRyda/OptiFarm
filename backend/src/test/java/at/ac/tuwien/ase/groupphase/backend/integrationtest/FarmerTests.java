package at.ac.tuwien.ase.groupphase.backend.integrationtest;

import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.LoginDto;
import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.farmer.ChangePasswordDto;
import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.farmer.EditFarmerDto;
import at.ac.tuwien.ase.groupphase.backend.integrationtest.steps.LoginStep;
import at.ac.tuwien.ase.groupphase.backend.integrationtest.steps.base.Step;
import at.ac.tuwien.ase.groupphase.backend.integrationtest.steps.farmer.ChangePasswordOfLoggedInFarmerStep;
import at.ac.tuwien.ase.groupphase.backend.integrationtest.steps.farmer.DeleteLoggedInFarmerStep;
import at.ac.tuwien.ase.groupphase.backend.integrationtest.steps.farmer.EditLoggedInFarmerStep;
import at.ac.tuwien.ase.groupphase.backend.integrationtest.steps.farmer.GetLoggedInFarmerStep;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class FarmerTests extends OptiFarmBaseTest {

    private static final String EMAIL = "john@doe.com";
    private static final String FIRSTNAME = "John";
    private static final String LASTNAME = "Doe";

    private final LoginDto loginDto = new LoginDto(EMAIL, "password");

    @Autowired
    private EditLoggedInFarmerStep editFarmerStep;

    @Autowired
    private GetLoggedInFarmerStep getLoggedInFarmerStep;

    @Autowired
    private DeleteLoggedInFarmerStep deleteLoggedInFarmerStep;

    @Autowired
    private ChangePasswordOfLoggedInFarmerStep changePasswordOfLoggedInFarmerStep;

    @Test
    public void getLoggedInFarmerReturns200AndCorrectDataWhenLoggedIn() throws Exception {
        Step<LoginDto, EditFarmerDto> flow = loginWithOk()
            .then(ignoredToken -> getLoggedInFarmerStep.run(null,
                status().isOk(),
                jsonPath("$.firstname").value(FIRSTNAME),
                jsonPath("$.lastname").value(LASTNAME),
                jsonPath("$.email").value(EMAIL)));

        flow.run(loginDto);
    }

    @Test
    public void editLoggedInFarmerReturns200AndCorrectDataWhenLoggedIn() throws Exception {
        var editFarmerDto = new EditFarmerDto("Joe", "Dane", "joe@dane.com");

        loginWithOk().then(ignoredToken -> editFarmerStep.run(editFarmerDto,
            status().isOk(),
            jsonPath("$.firstname").value("Joe"),
            jsonPath("$.lastname").value("Dane"),
            jsonPath("$.email").value("joe@dane.com"))).run(loginDto);
    }

    @Test
    public void deleteLoggedInFarmerReturns200() throws Exception {
        Step<LoginDto, Boolean> flow = loginWithOk()
            .then(ignoredToken -> deleteLoggedInFarmerStep.run(null,
                status().isOk()));

        Boolean result = flow.run(loginDto);

        assertTrue(result);
    }

    @Test
    public void changePasswordOfLoggedInFarmerReturns200() throws Exception {
        var changePasswordDto = new ChangePasswordDto("newPassword", "password");

        Step<LoginDto, Boolean> flow = loginWithOk()
            .then(ignoredToken -> changePasswordOfLoggedInFarmerStep.run(changePasswordDto,
                status().isOk()));

        Boolean result = flow.run(loginDto);

        assertTrue(result);
    }

    @Test
    public void changePasswordOfLoggedInFarmerReturns500_whenOldPasswordDoesNotMatch() throws Exception {
        var changePasswordDto = new ChangePasswordDto("newPassword", "oldPassword");

        Step<LoginDto, Boolean> flow = loginWithOk()
            .then(ignoredToken -> changePasswordOfLoggedInFarmerStep.run(changePasswordDto,
                status().isInternalServerError()));

        Boolean result = flow.run(loginDto);

        assertFalse(result);
    }
}
