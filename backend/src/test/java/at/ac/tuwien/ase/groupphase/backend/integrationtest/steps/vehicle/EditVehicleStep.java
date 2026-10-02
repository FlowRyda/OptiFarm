package at.ac.tuwien.ase.groupphase.backend.integrationtest.steps.vehicle;

import at.ac.tuwien.ase.groupphase.backend.configuration.TestFlowDataSection;
import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.VehicleDto;
import at.ac.tuwien.ase.groupphase.backend.integrationtest.steps.base.MockMvcStep;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

@Component
public class EditVehicleStep extends MockMvcStep<EditVehicleInput, VehicleDto> {

    public EditVehicleStep(MockMvc mockMvc, ObjectMapper objectMapper) {
        super(mockMvc, objectMapper);
    }

    @Override
    protected MockHttpServletRequestBuilder buildRequest(EditVehicleInput input) throws Exception {
        String token = TestFlowDataSection.current().getToken();

        return put("/api/v1/vehicle/" + input.id())
            .header("Authorization", token)
            .content(objectMapper.writeValueAsString(input.vehicleDto()))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON);
    }

    @Override
    protected VehicleDto parseResponse(String body) throws Exception {
        return objectMapper.readValue(body, VehicleDto.class);
    }
}
