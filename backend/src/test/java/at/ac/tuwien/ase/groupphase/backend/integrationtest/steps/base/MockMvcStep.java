package at.ac.tuwien.ase.groupphase.backend.integrationtest.steps.base;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

public abstract class MockMvcStep<Input, Output> implements Step<Input, Output> {

    protected final MockMvc mockMvc;
    protected final ObjectMapper objectMapper;

    protected MockMvcStep(MockMvc mockMvc, ObjectMapper objectMapper) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
    }

    protected abstract MockHttpServletRequestBuilder buildRequest(Input input) throws Exception;

    protected abstract Output parseResponse(String body) throws Exception;

    protected ResultMatcher[] defaultMatchers() {
        return new ResultMatcher[0];
    }

    @Override
    public Output run(Input input) throws Exception {
        ResultActions actions = mockMvc.perform(buildRequest(input));

        ResultMatcher[] matchers = defaultMatchers();
        if (matchers != null && matchers.length > 0) {
            actions.andExpectAll(matchers);
        }

        String body = actions.andReturn().getResponse().getContentAsString();
        if (body == null || body.isBlank()) {
            return null;
        }

        return parseResponse(body);
    }


    public Output run(Input input, ResultMatcher... extraMatchers) throws Exception {
        ResultActions actions = mockMvc.perform(buildRequest(input));

        ResultMatcher[] base = defaultMatchers();
        ResultMatcher[] all = mergeMatchers(base, extraMatchers);

        if (all.length > 0) {
            actions.andExpectAll(all);
        }

        String body = actions.andReturn().getResponse().getContentAsString();
        if (body == null || body.isBlank()) {
            return null;
        }

        return parseResponse(body);
    }

    private ResultMatcher[] mergeMatchers(ResultMatcher[] base, ResultMatcher[] extra) {
        if (base == null || base.length == 0) {
            return extra != null ? extra : new ResultMatcher[0];
        }
        if (extra == null || extra.length == 0) {
            return base;
        }

        ResultMatcher[] merged = new ResultMatcher[base.length + extra.length];
        System.arraycopy(base, 0, merged, 0, base.length);
        System.arraycopy(extra, 0, merged, base.length, extra.length);
        return merged;
    }
}
