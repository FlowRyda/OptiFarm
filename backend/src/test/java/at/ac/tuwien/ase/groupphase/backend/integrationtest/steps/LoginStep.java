package at.ac.tuwien.ase.groupphase.backend.integrationtest.steps;

import at.ac.tuwien.ase.groupphase.backend.configuration.TestFlowDataSection;
import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.LoginDto;
import at.ac.tuwien.ase.groupphase.backend.integrationtest.steps.base.MockMvcStep;
import at.ac.tuwien.ase.groupphase.backend.integrationtest.steps.base.Step;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Component
public class LoginStep extends MockMvcStep<LoginDto, String> {

    private static final String URL = "/api/v1/authentication";

    @Autowired
    public LoginStep(MockMvc mockMvc, ObjectMapper objectMapper) {
        super(mockMvc, objectMapper);
    }

    @Override
    protected MockHttpServletRequestBuilder buildRequest(LoginDto input) throws Exception {
        return post(URL)
            .content(objectMapper.writeValueAsString(input))
            .contentType(MediaType.APPLICATION_JSON);
    }

    @Override
    protected String parseResponse(String body) {
        TestFlowDataSection.current().setToken(body);
        return body;
    }
}
