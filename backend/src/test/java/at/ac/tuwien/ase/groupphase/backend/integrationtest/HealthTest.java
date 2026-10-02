package at.ac.tuwien.ase.groupphase.backend.integrationtest;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;


public class HealthTest extends OptiFarmBaseTest {

    /*
    It is very important that /health returns HTTP 200 for the k8s deployment. Otherwise, the deployment will be considered unhealthy
     and be terminated and restarted. This would waste resources of the shared k8s cluster which is not tolerated.

     In case you want to secure your endpoints (e.g. via spring web-security), define an exception for /health. It must be publicly accessible
     without any form of authentication in our infrastructure.
     */
    @Test
    public void getHealthReturns200() throws Exception {
        MvcResult mvcResult = this.mockMvc.perform(get("/health"))
            .andReturn();

        assertEquals(HttpStatus.OK.value(), mvcResult.getResponse().getStatus());
    }
}
