package at.ac.tuwien.ase.groupphase.backend.integrationtest.steps.task;

import at.ac.tuwien.ase.groupphase.backend.configuration.TestFlowDataSection;
import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.CoordinateDto;
import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.ToolCategoryDto;
import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.ToolDto;
import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.field.FieldCreateDto;
import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.field.FieldDetailDto;
import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.garage.GarageDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Component
public class CreateTaskPrerequisitesStep {

    private static final String GARAGE_URL = "/api/v1/garages/create";
    private static final String TOOL_URL = "/api/v1/tool";
    private static final String FIELD_URL = "/api/v1/field";

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;

    public CreateTaskPrerequisitesStep(MockMvc mockMvc, ObjectMapper objectMapper) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
    }

    public TaskPrerequisitesDto create() throws Exception {
        GarageDto garage = createGarage();
        ToolDto tool = createTool(garage);
        ToolCategoryDto category = tool.getCategoryDto();
        FieldDetailDto field = createField();
        return new TaskPrerequisitesDto(field, tool, category, garage);
    }

    private GarageDto createGarage() throws Exception {
        String token = TestFlowDataSection.current().getToken();

        GarageDto dto = new GarageDto();
        dto.setId(null);
        dto.setOwnerId(null);
        dto.setName("Test Garage");
        dto.setCoordinate(new CoordinateDto(10.0, 10.0));

        String responseJson = mockMvc.perform(
                post(GARAGE_URL)
                    .header("Authorization", token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dto))
            )
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString(StandardCharsets.UTF_8);

        return objectMapper.readValue(responseJson, GarageDto.class);
    }

    private ToolDto createTool(GarageDto garage) throws Exception {
        String token = TestFlowDataSection.current().getToken();

        ToolCategoryDto categoryDto = new ToolCategoryDto(null, "Test Category");

        ToolDto dto = new ToolDto();
        dto.setId(null);
        dto.setName("Test Tool");
        dto.setDescription("Test Tool Description");
        dto.setRemainingValue(100.0);
        dto.setCategoryDto(categoryDto);
        dto.setGarageDto(new GarageDto(garage.getId(), garage.getOwnerId(), garage.getName(), garage.getCoordinate()));
        dto.setLocationDto(new CoordinateDto(10.0, 10.0));
        dto.setUsable(true);
        dto.setImage(null);

        String responseJson = mockMvc.perform(
                post(TOOL_URL)
                    .header("Authorization", token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dto))
            )
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString(StandardCharsets.UTF_8);

        return objectMapper.readValue(responseJson, ToolDto.class);
    }

    private FieldDetailDto createField() throws Exception {
        String token = TestFlowDataSection.current().getToken();

        List<CoordinateDto> boundaries = List.of(
            new CoordinateDto(10.0, 10.0),
            new CoordinateDto(10.0, 10.001),
            new CoordinateDto(10.001, 10.001),
            new CoordinateDto(10.001, 10.0),
            new CoordinateDto(10.0, 10.0)
        );

        FieldCreateDto dto = new FieldCreateDto("Test Field", boundaries, null);

        String responseJson = mockMvc.perform(
                post(FIELD_URL)
                    .header("Authorization", token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dto))
            )
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString(StandardCharsets.UTF_8);

        return objectMapper.readValue(responseJson, FieldDetailDto.class);
    }
}
