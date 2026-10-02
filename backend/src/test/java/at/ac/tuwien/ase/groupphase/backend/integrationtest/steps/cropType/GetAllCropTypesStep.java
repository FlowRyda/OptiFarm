package at.ac.tuwien.ase.groupphase.backend.integrationtest.steps.cropType;

import at.ac.tuwien.ase.groupphase.backend.configuration.TestFlowDataSection;
import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.CropTypeDto;
import at.ac.tuwien.ase.groupphase.backend.integrationtest.steps.base.MockMvcStep;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@Component
public class GetAllCropTypesStep extends MockMvcStep<Object, List<CropTypeDto>> {

    public GetAllCropTypesStep(MockMvc mockMvc, ObjectMapper objectMapper) {
        super(mockMvc, objectMapper);
    }

    @Override
    protected MockHttpServletRequestBuilder buildRequest(Object ignored) {
        String token = TestFlowDataSection.current().getToken();

        return get("/api/v1/croptype")
            .header("Authorization", token)
            .accept(MediaType.APPLICATION_JSON);
    }

    @Override
    protected List<CropTypeDto> parseResponse(String body) throws Exception {
        return objectMapper.readValue(body, new TypeReference<>() {
        });
    }
}