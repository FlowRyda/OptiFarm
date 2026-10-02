package at.ac.tuwien.ase.groupphase.backend.integrationtest;

import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.CropTypeDto;
import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.LoginDto;
import at.ac.tuwien.ase.groupphase.backend.integrationtest.steps.base.Step;
import at.ac.tuwien.ase.groupphase.backend.integrationtest.steps.cropType.CreateCropTypeStep;
import at.ac.tuwien.ase.groupphase.backend.integrationtest.steps.cropType.DeleteCropTypeStep;
import at.ac.tuwien.ase.groupphase.backend.integrationtest.steps.cropType.EditCropTypeInput;
import at.ac.tuwien.ase.groupphase.backend.integrationtest.steps.cropType.EditCropTypeStep;
import at.ac.tuwien.ase.groupphase.backend.integrationtest.steps.cropType.GetAllCropTypesStep;
import at.ac.tuwien.ase.groupphase.backend.integrationtest.steps.cropType.GetCropTypeStep;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class CropTypeTests extends OptiFarmBaseTest {

    @Autowired
    private CreateCropTypeStep createCropTypeStep;

    @Autowired
    private EditCropTypeStep editCropTypeStep;

    @Autowired
    private DeleteCropTypeStep deleteCropTypeStep;

    @Autowired
    private GetAllCropTypesStep getAllCropTypesStep;

    @Autowired
    private GetCropTypeStep getCropTypeStep;

    @Test
    void createAndVerifyCropType() throws Exception {
        var newCropType = new CropTypeDto(null, "Wheat", 5);

        Step<LoginDto, CropTypeDto> fullFlow =
            loginWithOk()
                .then(ignored -> createCropTypeStep.run(newCropType, status().isCreated()))
                .then(created -> {
                    CropTypeDto fetched = getCropTypeStep.run(created.getId(), status().isOk());
                    assertTrue(fetched.getName().equals("Wheat"), "Fetched crop type should have the correct name");
                    assertTrue(fetched.getGrowthDuration() == 5, "Fetched crop type should have the correct growthDuration");
                    return created;
                })
                .then(created -> {
                    List<CropTypeDto> allCropTypes = getAllCropTypesStep.run(null, status().isOk());
                    boolean found = allCropTypes.stream()
                        .anyMatch(c -> c.getId().equals(created.getId()) && c.getName().equals("Wheat"));
                    assertTrue(found, "Created crop type should appear in getAllCropTypes");
                    return created;
                });

        CropTypeDto created = fullFlow.run(loginDto);

        deleteCropTypeStep.run(created.getId(), status().isNoContent());
    }

    @Test
    void editAndDeleteCropType() throws Exception {
        var newCropType = new CropTypeDto(null, "Corn", 4);
        CropTypeDto created = loginWithOk()
            .then(ignored -> createCropTypeStep.run(newCropType, status().isCreated()))
            .run(loginDto);

        var editedInput = new EditCropTypeInput(
            created.getId(),
            new CropTypeDto(created.getId(), "Maize", 5)
        );

        Step<LoginDto, CropTypeDto> editFlow = loginWithOk()
            .then(ignored -> editCropTypeStep.run(editedInput, status().isOk()))
            .then(updated -> {
                CropTypeDto fetched = getCropTypeStep.run(updated.getId(), status().isOk());
                assertTrue(fetched.getName().equals("Maize"), "Fetched crop type should have updated name");
                assertTrue(fetched.getGrowthDuration() == 5, "Fetched crop type should have updated growthDuration");
                return updated;
            });

        CropTypeDto updated = editFlow.run(loginDto);

        deleteCropTypeStep.run(updated.getId(), status().isNoContent());
    }


}
