package at.ac.tuwien.ase.groupphase.backend.integrationtest.steps.task;

import at.ac.tuwien.ase.groupphase.backend.configuration.TestFlowDataSection;
import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.schedule.TaskDto;
import at.ac.tuwien.ase.groupphase.backend.integrationtest.steps.base.MockMvcStep;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@Component
public class CreateTaskStep extends MockMvcStep<TaskDto, TaskDto> {

    public CreateTaskStep(MockMvc mockMvc, ObjectMapper objectMapper) {
        super(mockMvc, objectMapper);
    }

    @Override
    protected MockHttpServletRequestBuilder buildRequest(TaskDto dto) throws Exception {
        String token = TestFlowDataSection.current().getToken();

        return post("/api/v1/task")
            .header("Authorization", token)
            .content(objectMapper.writeValueAsString(dto))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON);
    }

    @Override
    protected TaskDto parseResponse(String body) throws Exception {
        if (body == null || body.isBlank()) {
            return null;
        }

        try {
            return objectMapper.readValue(body, TaskDto.class);
        } catch (JsonProcessingException e) {
            return null;
        }
    }
}