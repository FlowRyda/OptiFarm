package at.ac.tuwien.ase.groupphase.backend.integrationtest;

import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.CropTypeDto;
import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.SeedDto;
import at.ac.tuwien.ase.groupphase.backend.integrationtest.steps.cropType.CreateCropTypeStep;
import at.ac.tuwien.ase.groupphase.backend.integrationtest.steps.cropType.DeleteCropTypeStep;
import at.ac.tuwien.ase.groupphase.backend.integrationtest.steps.seed.CreateSeedStep;
import at.ac.tuwien.ase.groupphase.backend.integrationtest.steps.seed.DeleteSeedStep;
import at.ac.tuwien.ase.groupphase.backend.integrationtest.steps.seed.EditSeedInput;
import at.ac.tuwien.ase.groupphase.backend.integrationtest.steps.seed.EditSeedStep;
import at.ac.tuwien.ase.groupphase.backend.integrationtest.steps.seed.GetSeedStep;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class SeedTests extends OptiFarmBaseTest {

    @Autowired
    private CreateCropTypeStep createCropTypeStep;

    @Autowired
    private DeleteCropTypeStep deleteCropTypeStep;

    @Autowired
    private CreateSeedStep createSeedStep;

    @Autowired
    private EditSeedStep editSeedStep;

    @Autowired
    private DeleteSeedStep deleteSeedStep;

    @Autowired
    private GetSeedStep getSeedStep;

    @Test
    void createAndVerifySeed() throws Exception {
        var newCropType = new CropTypeDto(null, "Test Crop", 5);
        CropTypeDto createdCropType = loginWithOk()
            .then(ignored -> createCropTypeStep.run(newCropType, status().isCreated()))
            .run(loginDto);

        Long cropTypeId = createdCropType.getId();

        var newSeed = new SeedDto(
            null,
            cropTypeId,
            10.0,
            LocalDateTime.now(),
            "Sunflower Seed"
        );

        SeedDto createdSeed = loginWithOk()
            .then(ignored -> createSeedStep.run(newSeed, status().isCreated()))
            .run(loginDto);

        SeedDto fetched = getSeedStep.run(createdSeed.getId(), status().isOk());
        assertTrue(fetched.getName().equals("Sunflower Seed"));
        assertTrue(fetched.getBuyingPrice() == 10.0);
        assertTrue(fetched.getCropTypeId().equals(cropTypeId));

        deleteSeedStep.run(createdSeed.getId(), status().isNoContent());
        deleteCropTypeStep.run(createdCropType.getId(), status().isNoContent());
    }

    @Test
    void editAndDeleteSeed() throws Exception {
        var cropType = new CropTypeDto(null, "Edit Crop", 6);
        CropTypeDto createdCropType = loginWithOk()
            .then(ignored -> createCropTypeStep.run(cropType, status().isCreated()))
            .run(loginDto);

        Long cropTypeId = createdCropType.getId();

        var seed = new SeedDto(
            null,
            cropTypeId,
            15.0,
            LocalDateTime.now(),
            "Corn Seed"
        );

        SeedDto createdSeed = loginWithOk()
            .then(ignored -> createSeedStep.run(seed, status().isCreated()))
            .run(loginDto);

        var editedSeedInput = new EditSeedInput(
            createdSeed.getId(),
            new SeedDto(createdSeed.getId(), cropTypeId, 20.0, LocalDateTime.now(), "Maize Seed")
        );

        SeedDto updatedSeed = loginWithOk()
            .then(ignored -> editSeedStep.run(editedSeedInput, status().isOk()))
            .then(updated -> {
                SeedDto fetched = getSeedStep.run(updated.getId(), status().isOk());
                assertTrue(fetched.getName().equals("Maize Seed"));
                assertTrue(fetched.getBuyingPrice() == 20.0);
                return updated;
            })
            .run(loginDto);

        deleteSeedStep.run(updatedSeed.getId(), status().isNoContent());
        deleteCropTypeStep.run(createdCropType.getId(), status().isNoContent());
    }


}
