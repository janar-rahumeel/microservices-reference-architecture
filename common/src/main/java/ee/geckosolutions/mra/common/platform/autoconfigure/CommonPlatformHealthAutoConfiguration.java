package ee.geckosolutions.mra.common.platform.autoconfigure;

import ee.geckosolutions.mra.common.platform.config.PlatformProperties;
import ee.geckosolutions.mra.common.platform.observation.CriticalComponentHealthMetrics;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.boot.health.actuate.endpoint.HealthEndpoint;
import org.springframework.boot.health.autoconfigure.actuate.endpoint.HealthEndpointAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.core.type.AnnotatedTypeMetadata;

@AutoConfiguration(after = HealthEndpointAutoConfiguration.class)
public class CommonPlatformHealthAutoConfiguration {

    @Bean
    @Conditional(CriticalHealthGroupCondition.class)
    CriticalComponentHealthMetrics criticalComponentHealthMetrics(
            PlatformProperties platformProperties,
            HealthEndpoint healthEndpoint,
            MeterRegistry meterRegistry) {
        return new CriticalComponentHealthMetrics(platformProperties, healthEndpoint, meterRegistry);
    }

    private static class CriticalHealthGroupCondition extends SpringBootCondition {

        private static final String PROPERTY_KEY = "management.endpoint.health.group.critical.include";

        @Override
        public ConditionOutcome getMatchOutcome(
                ConditionContext conditionContext,
                AnnotatedTypeMetadata annotatedTypeMetadata) {
            String[] values = conditionContext.getEnvironment().getProperty(PROPERTY_KEY, String[].class);

            if (values != null && values.length > 0) {
                return ConditionOutcome.match("Critical health group is configured");
            }

            return ConditionOutcome.noMatch("Critical health group is not configured");
        }

    }

}
