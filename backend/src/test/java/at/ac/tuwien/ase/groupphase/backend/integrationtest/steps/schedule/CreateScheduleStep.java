package at.ac.tuwien.ase.groupphase.backend.integrationtest.steps.schedule;

import at.ac.tuwien.ase.groupphase.backend.configuration.TestFlowDataSection;
import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.schedule.ScheduleDto;
import at.ac.tuwien.ase.groupphase.backend.integrationtest.steps.base.MockMvcStep;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@Component
public class CreateScheduleStep extends MockMvcStep<Void, ScheduleDto> {

    public CreateScheduleStep(MockMvc mockMvc, ObjectMapper objectMapper) {
        super(mockMvc, objectMapper);
    }

    @Override
    protected MockHttpServletRequestBuilder buildRequest(Void ignored) {
        String token = TestFlowDataSection.current().getToken();

        return post("/api/v1/schedules")
            .header("Authorization", token)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON);
    }

    @Override
    protected ScheduleDto parseResponse(String body) throws Exception {
        if (body == null || body.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readValue(body, ScheduleDto.class);
        } catch (JsonProcessingException e) {
            return null;
        }
    }
}