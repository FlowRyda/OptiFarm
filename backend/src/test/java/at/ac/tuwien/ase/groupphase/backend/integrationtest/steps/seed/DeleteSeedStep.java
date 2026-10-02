package at.ac.tuwien.ase.groupphase.backend.integrationtest.steps.seed;

import at.ac.tuwien.ase.groupphase.backend.configuration.TestFlowDataSection;
import at.ac.tuwien.ase.groupphase.backend.integrationtest.steps.base.MockMvcStep;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;

@Component
public class DeleteSeedStep extends MockMvcStep<Long, Boolean> {

    public DeleteSeedStep(MockMvc mockMvc, ObjectMapper objectMapper) {
        super(mockMvc, objectMapper);
    }

    @Override
    protected MockHttpServletRequestBuilder buildRequest(Long id) {
        String token = TestFlowDataSection.current().getToken();

        return delete("/api/v1/seeds/delete/" + id)
            .header("Authorization", token)
            .accept(MediaType.APPLICATION_JSON);
    }

    @Override
    protected Boolean parseResponse(String body) {
        return true;
    }
}