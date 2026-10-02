package at.ac.tuwien.ase.groupphase.backend.integrationtest.steps.farmer;

import at.ac.tuwien.ase.groupphase.backend.configuration.TestFlowDataSection;
import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.farmer.EditFarmerDto;
import at.ac.tuwien.ase.groupphase.backend.integrationtest.steps.base.MockMvcStep;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Component
public class GetLoggedInFarmerStep extends MockMvcStep<Object, EditFarmerDto> {

    public GetLoggedInFarmerStep(MockMvc mockMvc, ObjectMapper objectMapper) {
        super(mockMvc, objectMapper);
    }

    @Override
    protected MockHttpServletRequestBuilder buildRequest(Object ignored) {
        var token = TestFlowDataSection.current().getToken();
        
        return get("/api/v1/farmers")
            .header("Authorization", token)
            .accept(MediaType.APPLICATION_JSON);
    }

    @Override
    protected EditFarmerDto parseResponse(String body) throws Exception {
        return objectMapper.readValue(body, EditFarmerDto.class);
    }

    @Override
    protected ResultMatcher[] defaultMatchers() {
        return new ResultMatcher[]{status().isOk()};
    }
}
