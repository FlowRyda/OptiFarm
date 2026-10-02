package at.ac.tuwien.ase.groupphase.backend.integrationtest;


import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.CoordinateDto;
import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.LoginDto;
import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.garage.GarageDto;
import at.ac.tuwien.ase.groupphase.backend.integrationtest.steps.base.Step;
import at.ac.tuwien.ase.groupphase.backend.integrationtest.steps.garage.CreateGarageStep;
import at.ac.tuwien.ase.groupphase.backend.integrationtest.steps.garage.DeleteGarageStep;
import at.ac.tuwien.ase.groupphase.backend.integrationtest.steps.garage.EditGarageInput;
import at.ac.tuwien.ase.groupphase.backend.integrationtest.steps.garage.EditGarageStep;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class GarageTests extends OptiFarmBaseTest {
    @Autowired

    private CreateGarageStep createGarageStep;
    @Autowired

    private EditGarageStep editGarageStep;
    @Autowired
    private DeleteGarageStep deleteGarageStep;


    @Test
    void createEditAndDeleteGarageFlow() throws Exception {
        var newGarage = new GarageDto(null, null, "My Garage", new CoordinateDto(0, 0));

        Step<LoginDto, Boolean> fullFlow =
            loginWithOk()
                .then(ignored -> createGarageStep.run(newGarage, status().isOk()))
                .then(createdGarage -> {
                    var edited = new EditGarageInput(
                        createdGarage.getId(),
                        new GarageDto(createdGarage.getId(), createdGarage.getOwnerId(), "New Name", new CoordinateDto(1, 1))
                    );

                    return editGarageStep.run(edited, status().isOk());
                })
                .then(updated -> deleteGarageStep.run(
                    updated.getId(),
                    status().isOk()
                ));

        Boolean deleted = fullFlow.run(loginDto);
        assertNull(deleted);
    }
}
