package ee.geckosolutions.mra.common.platform.config;

import java.time.Duration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "application.platform")
public class PlatformProperties {

    private final CriticalComponentHealthMetrics criticalComponentHealthMetrics = new CriticalComponentHealthMetrics();

    @Getter
    @Setter
    public static class CriticalComponentHealthMetrics {

        private Duration refreshInterval = Duration.ofSeconds(10);

    }

}
