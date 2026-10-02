package at.ac.tuwien.ase.groupphase.backend.integrationtest;

import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.CoordinateDto;
import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.ToolCategoryDto;
import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.VehicleDto;
import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.garage.GarageDto;
import at.ac.tuwien.ase.groupphase.backend.integrationtest.steps.garage.CreateGarageStep;
import at.ac.tuwien.ase.groupphase.backend.integrationtest.steps.vehicle.CreateVehicleStep;
import at.ac.tuwien.ase.groupphase.backend.integrationtest.steps.vehicle.DeleteVehicleStep;
import at.ac.tuwien.ase.groupphase.backend.integrationtest.steps.vehicle.EditVehicleInput;
import at.ac.tuwien.ase.groupphase.backend.integrationtest.steps.vehicle.EditVehicleStep;
import at.ac.tuwien.ase.groupphase.backend.integrationtest.steps.vehicle.GetAllVehiclesStep;
import at.ac.tuwien.ase.groupphase.backend.integrationtest.steps.vehicle.GetVehicleStep;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class VehicleTests extends OptiFarmBaseTest {
    @Autowired
    private CreateVehicleStep createVehicleStep;
    @Autowired
    private GetVehicleStep getVehicleStep;
    @Autowired
    private GetAllVehiclesStep getAllVehiclesStep;
    @Autowired
    private EditVehicleStep editVehicleStep;
    @Autowired
    private DeleteVehicleStep deleteVehicleStep;
    @Autowired
    private CreateGarageStep createGarageStep;

    @Test
    void createVehicle_returns200_andVehicle() throws Exception {
        loginWithOk().run(loginDto);

        GarageDto garage = createGarageFixture();
        VehicleDto newVehicle = createTestVehicle(garage);

        VehicleDto created = createVehicleStep.run(newVehicle, status().isOk());

        assertNotNull(created);
        assertNotNull(created.getId());

        assertAll(
            () -> assertEquals(newVehicle.getName(), created.getName(), "name"),
            () -> assertEquals(newVehicle.getDescription(), created.getDescription(), "description"),
            () -> assertEquals(newVehicle.getRemainingValue(), created.getRemainingValue(), "remainingValue"),

            () -> assertEquals(newVehicle.getCategoryDto().getName(), created.getCategoryDto().getName(), "categoryDto.name"),
            () -> assertNotNull(created.getCategoryDto().getId(), "categoryDto.id should be generated"),

            () -> assertEquals(garage.getId(), created.getGarageDto().getId(), "garageDto.id"),
            () -> assertEquals(garage.getName(), created.getGarageDto().getName(), "garageDto.name"),

            () -> assertAll("locationDto",
                () -> assertEquals(newVehicle.getLocationDto().getLat(), created.getLocationDto().getLat(), 1e-6),
                () -> assertEquals(newVehicle.getLocationDto().getLon(), created.getLocationDto().getLon(), 1e-6)

            )
        );
    }

    @Test
    void getVehicle_returnsVehicleById() throws Exception {
        loginWithOk().run(loginDto);

        GarageDto garage = createGarageFixture();
        VehicleDto created = createVehicleStep.run(createTestVehicle(garage), status().isOk());

        VehicleDto fetched = getVehicleStep.run(created.getId(), status().isOk());

        assertNotNull(fetched);
        assertEquals(created.getId(), fetched.getId());
    }

    @Test
    void getAllVehicles_containsCreatedVehicle() throws Exception {
        loginWithOk().run(loginDto);

        GarageDto garage = createGarageFixture();
        VehicleDto created = createVehicleStep.run(createTestVehicle(garage), status().isOk());

        List<VehicleDto> all = getAllVehiclesStep.run(null, status().isOk());

        assertNotNull(all);
        assertFalse(all.isEmpty());
        assertTrue(all.stream().anyMatch(v -> v.getId().equals(created.getId())));
    }

    @Test
    void updateVehicle_updatesData() throws Exception {
        loginWithOk().run(loginDto);

        GarageDto garage = createGarageFixture();
        VehicleDto created = createVehicleStep.run(createTestVehicle(garage), status().isOk());

        VehicleDto updatedDto = createUpdatedVehicle(created);

        EditVehicleInput input = new EditVehicleInput(created.getId(), updatedDto);

        VehicleDto updated = editVehicleStep.run(input, status().isOk());

        assertNotNull(updated);
        assertEquals(created.getId(), updated.getId());

        assertAll(
            () -> assertEquals(created.getId(), updated.getId(), "id"),

            () -> assertEquals(updatedDto.getName(), updated.getName(), "name"),
            () -> assertEquals(updatedDto.getDescription(), updated.getDescription(), "description"),
            () -> assertEquals(updatedDto.getRemainingValue(), updated.getRemainingValue(), "remainingValue"),
            () -> assertEquals(updatedDto.isUsable(), updated.isUsable(), "isUsable"),
            () -> assertEquals(updatedDto.getImage(), updated.getImage(), "image"),
            () -> assertEquals(updatedDto.getFuelLevel(), updated.getFuelLevel(), "fuelLevel"),
            () -> assertEquals(updatedDto.getDrivenKm(), updated.getDrivenKm(), "drivenKm"),

            () -> assertEquals(updatedDto.getCategoryDto().getName(), updated.getCategoryDto().getName(), "category name"),
            () -> assertNotNull(updated.getCategoryDto().getId(), "category id should be set by backend"),

            () -> assertEquals(created.getGarageDto().getId(), updated.getGarageDto().getId(), "garage id should not change"),
            () -> assertEquals(created.getGarageDto().getName(), updated.getGarageDto().getName(), "garage name"),

            () -> assertEquals(updatedDto.getLocationDto().getLat(), updated.getLocationDto().getLat(), "location x"),
            () -> assertEquals(updatedDto.getLocationDto().getLon(), updated.getLocationDto().getLon(), "location y")
        );
    }

    @Test
    void deleteVehicle_returnsTrueAndRemovesVehicle() throws Exception {
        loginWithOk().run(loginDto);

        GarageDto garage = createGarageFixture();
        VehicleDto created = createVehicleStep.run(createTestVehicle(garage), status().isOk());

        Boolean deleted = deleteVehicleStep.run(created.getId(), status().isOk());

        assertNotNull(deleted);
        assertTrue(deleted);

        getVehicleStep.run(created.getId(), status().isNotFound());
    }

    @Test
    void createVehicle_withInvalidData_returns400() throws Exception {
        loginWithOk().run(loginDto);

        GarageDto garage = createGarageFixture();
        VehicleDto invalid = createTestVehicle(garage);

        invalid.setName(null);

        VehicleDto response = createVehicleStep.run(invalid, status().isBadRequest());

        assertNull(response);
    }

    @Test
    void createVehicle_withNonExistingGarage_returns404() throws Exception {
        loginWithOk().run(loginDto);

        GarageDto nonExistingGarage = createTestGarage();

        VehicleDto dto = createTestVehicle(nonExistingGarage);

        VehicleDto response = createVehicleStep.run(dto, status().isNotFound());

        assertNull(response);
    }

    private GarageDto createTestGarage() {
        return new GarageDto(
            null,
            null,
            "Main Garage",
            new CoordinateDto(0, 0)
        );
    }

    private GarageDto createGarageFixture() throws Exception {
        return createGarageStep.run(createTestGarage(), status().isOk());
    }

    private VehicleDto createTestVehicle(GarageDto garageDto) {
        VehicleDto dto = new VehicleDto();
        ToolCategoryDto toolCategoryDto = new ToolCategoryDto(null, "Tractor");

        dto.setId(null);
        dto.setName("TestVehicle");
        dto.setDescription("Test Vehicle Description");
        dto.setUsable(true);
        dto.setRemainingValue(100.40);
        dto.setCategoryDto(toolCategoryDto);
        dto.setGarageDto(garageDto);
        dto.setLocationDto(new CoordinateDto(0, 0));
        dto.setImage(null);
        dto.setFuelLevel(64.5);
        dto.setDrivenKm(35123);
        return dto;
    }

    private VehicleDto createUpdatedVehicle(VehicleDto dto) {
        ToolCategoryDto toolCategoryDto = new ToolCategoryDto(null, "Plow");
        dto.setName("UpdatedVehicle");
        dto.setImage("test image=");
        dto.setCategoryDto(toolCategoryDto);
        dto.setFuelLevel(59.2);
        dto.setDrivenKm(36512);
        return dto;
    }
}
