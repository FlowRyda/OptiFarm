package at.ac.tuwien.ase.groupphase.backend.integrationtest.steps.vehicle;

import at.ac.tuwien.ase.groupphase.backend.configuration.TestFlowDataSection;
import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.VehicleDto;
import at.ac.tuwien.ase.groupphase.backend.integrationtest.steps.base.MockMvcStep;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@Component
public class CreateVehicleStep extends MockMvcStep<VehicleDto, VehicleDto> {
    public CreateVehicleStep(MockMvc mockMvc, ObjectMapper objectMapper) {
        super(mockMvc, objectMapper);
    }

    @Override
    protected MockHttpServletRequestBuilder buildRequest(VehicleDto dto) throws Exception {
        String token = TestFlowDataSection.current().getToken();

        return post("/api/v1/vehicle")
            .header("Authorization", token)
            .content(objectMapper.writeValueAsString(dto))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON);
    }

    @Override
    protected VehicleDto parseResponse(String body) throws Exception {
        if (body == null || body.isBlank()) {
            return null;
        }

        try {
            return objectMapper.readValue(body, VehicleDto.class);
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            return null;
        }
    }

}
