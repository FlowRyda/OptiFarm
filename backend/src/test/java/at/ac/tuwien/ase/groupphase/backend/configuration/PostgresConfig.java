package at.ac.tuwien.ase.groupphase.backend.configuration;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
@ConditionalOnProperty(
    name = "it.use-testcontainers",
    havingValue = "true",
    matchIfMissing = true
)
public class PostgresConfig {
    @Bean
    @ServiceConnection
    public PostgreSQLContainer postgresDbContainer() {
        DockerImageName dockerImageName = DockerImageName.parse("postgis/postgis:16-3.4").asCompatibleSubstituteFor("postgres");
        return new PostgreSQLContainer(dockerImageName);
    }
}