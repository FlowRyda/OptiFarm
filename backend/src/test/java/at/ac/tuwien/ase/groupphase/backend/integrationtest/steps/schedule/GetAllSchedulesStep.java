package at.ac.tuwien.ase.groupphase.backend.integrationtest.steps.schedule;

import at.ac.tuwien.ase.groupphase.backend.configuration.TestFlowDataSection;
import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.schedule.ScheduleDto;
import at.ac.tuwien.ase.groupphase.backend.integrationtest.steps.base.MockMvcStep;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@Component
public class GetAllSchedulesStep extends MockMvcStep<Void, List<ScheduleDto>> {

    public GetAllSchedulesStep(MockMvc mockMvc, ObjectMapper objectMapper) {
        super(mockMvc, objectMapper);
    }

    @Override
    protected MockHttpServletRequestBuilder buildRequest(Void ignored) {
        String token = TestFlowDataSection.current().getToken();

        return get("/api/v1/schedules")
            .header("Authorization", token)
            .accept(MediaType.APPLICATION_JSON);
    }

    @Override
    protected List<ScheduleDto> parseResponse(String body) throws Exception {
        if (body == null || body.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(body, new TypeReference<List<ScheduleDto>>() {});
        } catch (JsonProcessingException e) {
            return List.of();
        }
    }
}