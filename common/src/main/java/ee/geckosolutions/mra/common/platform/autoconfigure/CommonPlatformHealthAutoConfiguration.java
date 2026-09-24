package ee.geckosolutions.mra.common.platform.autoconfigure;

import ee.geckosolutions.mra.common.platform.observation.CriticalComponentHealthMetrics;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.health.actuate.endpoint.HealthEndpoint;
import org.springframework.boot.health.actuate.endpoint.HealthEndpointGroups;
import org.springframework.boot.health.autoconfigure.actuate.endpoint.HealthEndpointAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@AutoConfiguration(after = HealthEndpointAutoConfiguration.class)
public class CommonPlatformHealthAutoConfiguration {

    @Bean
    CriticalComponentHealthMetrics criticalComponentHealthMetrics(
            HealthEndpoint healthEndpoint,
            HealthEndpointGroups healthEndpointGroups,
            MeterRegistry meterRegistry) {
        return new CriticalComponentHealthMetrics(healthEndpoint, healthEndpointGroups, meterRegistry);
    }

}
