package at.ac.tuwien.ase.groupphase.backend.integrationtest.steps.farmer;

import at.ac.tuwien.ase.groupphase.backend.configuration.TestFlowDataSection;
import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.farmer.ChangePasswordDto;
import at.ac.tuwien.ase.groupphase.backend.integrationtest.steps.base.MockMvcStep;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

@Component
public class ChangePasswordOfLoggedInFarmerStep
    extends MockMvcStep<ChangePasswordDto, Boolean> {

    public ChangePasswordOfLoggedInFarmerStep(MockMvc mockMvc, ObjectMapper objectMapper) {
        super(mockMvc, objectMapper);
    }

    @Override
    protected MockHttpServletRequestBuilder buildRequest(
        ChangePasswordDto dto) throws Exception {

        var token = TestFlowDataSection.current().getToken();

        return put("/api/v1/farmers/changePassword")
            .header("Authorization", token)
            .content(objectMapper.writeValueAsString(dto))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON);
    }

    @Override
    protected Boolean parseResponse(String body) {
        return Boolean.parseBoolean(body);
    }
}
