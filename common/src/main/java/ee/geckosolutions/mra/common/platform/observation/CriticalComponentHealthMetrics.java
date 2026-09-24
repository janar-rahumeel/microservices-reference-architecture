package ee.geckosolutions.mra.common.platform.observation;

import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.health.actuate.endpoint.CompositeHealthDescriptor;
import org.springframework.boot.health.actuate.endpoint.HealthDescriptor;
import org.springframework.boot.health.actuate.endpoint.HealthEndpoint;
import org.springframework.boot.health.actuate.endpoint.HealthEndpointGroups;
import org.springframework.boot.health.contributor.Status;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.concurrent.SimpleAsyncTaskScheduler;

@Slf4j
@RequiredArgsConstructor
public class CriticalComponentHealthMetrics implements ApplicationListener<ApplicationReadyEvent>, DisposableBean {

    private static final String GROUP = "critical";
    private static final String METRIC_NAME = "application_critical_component_up";

    private final Map<String, AtomicReference<Boolean>> componentHealth = new ConcurrentHashMap<>();
    private final SimpleAsyncTaskScheduler taskScheduler = simpleAsyncTaskScheduler();

    private final HealthEndpoint healthEndpoint;
    private final HealthEndpointGroups healthEndpointGroups;
    private final MeterRegistry meterRegistry;

    private Set<String> componentNames = Set.of();
    @Value("${application.common.critical-component-health-metrics.refresh-interval:10s}")
    private Duration refreshInterval;

    private static SimpleAsyncTaskScheduler simpleAsyncTaskScheduler() {
        SimpleAsyncTaskScheduler scheduler = new SimpleAsyncTaskScheduler();
        scheduler.setVirtualThreads(true);
        scheduler.setThreadNamePrefix("critical-health-");
        return scheduler;
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        if (healthEndpointGroups.get(GROUP) == null) {
            return;
        }

        componentNames = Optional.ofNullable(healthEndpoint.healthForPath(GROUP))
                .map(descriptor -> (CompositeHealthDescriptor) descriptor)
                .map(CompositeHealthDescriptor::getComponents)
                .map(Map::keySet)
                .map(Set::copyOf)
                .orElseGet(Set::of);

        if (componentNames.isEmpty()) {
            return;
        }

        componentNames.forEach(this::registerComponent);

        refreshComponentHealth();

        taskScheduler.scheduleWithFixedDelay(this::refreshComponentHealth, refreshInterval);
    }

    private void registerComponent(String componentName) {
        AtomicReference<Boolean> isUp = new AtomicReference<>(false);

        componentHealth.put(componentName, isUp);

        Gauge.builder(METRIC_NAME, isUp, status -> status.get() ? 1.0 : 0.0)
                .description("Health status of a critical component (1 = UP, 0 = not UP)")
                .tag("component", componentName)
                .register(meterRegistry);
    }

    public void refreshComponentHealth() {
        if (componentNames.isEmpty()) {
            return;
        }

        componentNames.forEach(componentName -> {
            AtomicReference<Boolean> healthReference = componentHealth.get(componentName);

            try {
                HealthDescriptor healthDescriptor = Objects.requireNonNull(healthEndpoint.healthForPath(componentName));
                healthReference.set(Status.UP.equals(healthDescriptor.getStatus()));
            } catch (Exception e) {
                healthReference.set(false);
                log.warn("Health check failed for component {}, setting its UP status to false", componentName, e);
            }
        });
    }

    @Override
    public void destroy() {
        taskScheduler.close();
    }

}
