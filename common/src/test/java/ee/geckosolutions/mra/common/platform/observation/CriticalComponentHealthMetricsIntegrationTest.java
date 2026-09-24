package ee.geckosolutions.mra.common.platform.observation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.sql.SQLException;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.h2.tools.Server;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@Slf4j
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT,
        properties = {
                "spring.datasource.username=sa",
                "spring.datasource.password=",
                "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
                "spring.datasource.hikari.connection-timeout=1000",
                "management.endpoint.health.group.critical.include=db",
                "application.common.critical-component-health-metrics.refresh-interval=1s" })
class CriticalComponentHealthMetricsIntegrationTest {

    @Nullable
    private static Server H2_SERVER;

    @Autowired
    private MeterRegistry meterRegistry;

    @DynamicPropertySource
    static void datasourceProperties(DynamicPropertyRegistry registry) throws SQLException {
        H2_SERVER = Server.createTcpServer("-tcp", "-tcpPort", "0", "-ifNotExists").start();

        registry.add("spring.datasource.url", () -> "jdbc:h2:tcp://localhost:" + H2_SERVER.getPort() + "/~/test");
    }

    @AfterAll
    static void afterAll() {
        if (H2_SERVER != null) {
            H2_SERVER.stop();
        }
    }

    @Test
    void shouldReportDatabaseDownWhenH2IsStopped() {
        // given
        await().untilAsserted(
                () -> assertThat(meterRegistry.get("application_critical_component_up").tag("component", "db").gauge().value())
                        .isEqualTo(1.0));

        // when
        H2_SERVER.stop();

        // then
        await().untilAsserted(
                () -> assertThat(meterRegistry.get("application_critical_component_up").tag("component", "db").gauge().value())
                        .isEqualTo(0.0));
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    static class TestApplication {
    }

}
