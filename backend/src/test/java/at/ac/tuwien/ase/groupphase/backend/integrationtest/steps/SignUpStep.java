package at.ac.tuwien.ase.groupphase.backend.integrationtest.steps;

import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.farmer.CreateFarmerDto;
import at.ac.tuwien.ase.groupphase.backend.integrationtest.steps.base.MockMvcStep;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@Component
public class SignUpStep extends MockMvcStep<CreateFarmerDto, Boolean> {

    public SignUpStep(MockMvc mockMvc, ObjectMapper objectMapper) {
        super(mockMvc, objectMapper);
    }

    @Override
    protected MockHttpServletRequestBuilder buildRequest(CreateFarmerDto dto) throws Exception {
        return post("/api/v1/signup")
            .content(objectMapper.writeValueAsString(dto))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON);
    }

    @Override
    protected Boolean parseResponse(String body) {
        return true;
    }

    @Override
    protected ResultMatcher[] defaultMatchers() {
        return new ResultMatcher[0];
    }
}
