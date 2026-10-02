package at.ac.tuwien.ase.groupphase.backend.integrationtest.steps.vehicle;

import at.ac.tuwien.ase.groupphase.backend.configuration.TestFlowDataSection;
import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.VehicleDto;
import at.ac.tuwien.ase.groupphase.backend.integrationtest.steps.base.MockMvcStep;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@Component
public class GetVehicleStep extends MockMvcStep<Long, VehicleDto> {

    public GetVehicleStep(MockMvc mockMvc, ObjectMapper objectMapper) {
        super(mockMvc, objectMapper);
    }

    @Override
    protected MockHttpServletRequestBuilder buildRequest(Long id) {
        String token = TestFlowDataSection.current().getToken();

        return get("/api/v1/vehicle/" + id)
            .header("Authorization", token)
            .accept(MediaType.APPLICATION_JSON);
    }

    @Override
    protected VehicleDto parseResponse(String body) throws Exception {
        if (body == null || body.isBlank()) {
            return null;
        }

        try {
            return objectMapper.readValue(body, VehicleDto.class);
        } catch (JsonProcessingException e) {
            return null;
        }
    }
}
