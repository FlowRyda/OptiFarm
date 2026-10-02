package at.ac.tuwien.ase.groupphase.backend.integrationtest.steps.vehicle;

import at.ac.tuwien.ase.groupphase.backend.configuration.TestFlowDataSection;
import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.VehicleDto;
import at.ac.tuwien.ase.groupphase.backend.integrationtest.steps.base.MockMvcStep;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@Component
public class GetAllVehiclesStep extends MockMvcStep<Object, List<VehicleDto>> {

    public GetAllVehiclesStep(MockMvc mockMvc, ObjectMapper objectMapper) {
        super(mockMvc, objectMapper);
    }

    @Override
    protected MockHttpServletRequestBuilder buildRequest(Object ignored) {
        String token = TestFlowDataSection.current().getToken();

        return get("/api/v1/vehicle")
            .header("Authorization", token)
            .accept(MediaType.APPLICATION_JSON);
    }

    @Override
    protected List<VehicleDto> parseResponse(String body) throws Exception {
        return objectMapper.readValue(body, new TypeReference<>() {
        });
    }
}
