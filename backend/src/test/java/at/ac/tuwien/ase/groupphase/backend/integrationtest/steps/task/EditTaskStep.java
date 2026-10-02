package at.ac.tuwien.ase.groupphase.backend.integrationtest.steps.task;

import at.ac.tuwien.ase.groupphase.backend.configuration.TestFlowDataSection;
import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.schedule.TaskDto;
import at.ac.tuwien.ase.groupphase.backend.integrationtest.steps.base.MockMvcStep;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

@Component
public class EditTaskStep extends MockMvcStep<EditTaskInput, TaskDto> {

    public EditTaskStep(MockMvc mockMvc, ObjectMapper objectMapper) {
        super(mockMvc, objectMapper);
    }

    @Override
    protected MockHttpServletRequestBuilder buildRequest(EditTaskInput input) throws Exception {
        String token = TestFlowDataSection.current().getToken();

        return put("/api/v1/task/" + input.id())
            .header("Authorization", token)
            .content(objectMapper.writeValueAsString(input.taskDto()))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON);
    }

    @Override
    protected TaskDto parseResponse(String body) throws Exception {
        return objectMapper.readValue(body, TaskDto.class);
    }
}