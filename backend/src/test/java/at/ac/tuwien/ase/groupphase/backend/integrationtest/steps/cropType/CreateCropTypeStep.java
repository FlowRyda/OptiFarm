package at.ac.tuwien.ase.groupphase.backend.integrationtest.steps.cropType;

import at.ac.tuwien.ase.groupphase.backend.configuration.TestFlowDataSection;
import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.CropTypeDto;
import at.ac.tuwien.ase.groupphase.backend.integrationtest.steps.base.MockMvcStep;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@Component
public class CreateCropTypeStep extends MockMvcStep<CropTypeDto, CropTypeDto> {

    public CreateCropTypeStep(MockMvc mockMvc, ObjectMapper objectMapper) {
        super(mockMvc, objectMapper);
    }

    @Override
    protected MockHttpServletRequestBuilder buildRequest(CropTypeDto dto) throws Exception {
        String token = TestFlowDataSection.current().getToken();

        return post("/api/v1/croptype/create")
            .header("Authorization", token)
            .content(objectMapper.writeValueAsString(dto))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON);
    }

    @Override
    protected CropTypeDto parseResponse(String body) throws Exception {
        return objectMapper.readValue(body, CropTypeDto.class);
    }
}
