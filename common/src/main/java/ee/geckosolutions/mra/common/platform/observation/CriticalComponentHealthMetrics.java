package ee.geckosolutions.mra.common.platform.observation;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import ee.geckosolutions.mra.common.platform.config.PlatformProperties;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.health.actuate.endpoint.CompositeHealthDescriptor;
import org.springframework.boot.health.actuate.endpoint.HealthDescriptor;
import org.springframework.boot.health.actuate.endpoint.HealthEndpoint;
import org.springframework.boot.health.contributor.Status;
import org.springframework.context.SmartLifecycle;
import org.springframework.scheduling.concurrent.SimpleAsyncTaskScheduler;

@Slf4j
@RequiredArgsConstructor
public class CriticalComponentHealthMetrics implements SmartLifecycle {

    private static final String GROUP = "critical";
    private static final String METRIC_NAME = "application_critical_component_up";

    private final Map<String, AtomicBoolean> componentHealth = new ConcurrentHashMap<>();
    private final SimpleAsyncTaskScheduler taskScheduler = simpleAsyncTaskScheduler();
    private final ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor();
    private final AtomicBoolean running = new AtomicBoolean();
    private final AtomicBoolean refreshRunning = new AtomicBoolean();

    private final PlatformProperties platformProperties;
    private final HealthEndpoint healthEndpoint;
    private final MeterRegistry meterRegistry;

    private Set<String> componentNames = Set.of();

    private static SimpleAsyncTaskScheduler simpleAsyncTaskScheduler() {
        SimpleAsyncTaskScheduler scheduler = new SimpleAsyncTaskScheduler();
        scheduler.setVirtualThreads(true);
        scheduler.setThreadNamePrefix("critical-health-");
        return scheduler;
    }

    @Override
    public boolean isRunning() {
        return running.get();
    }

    @Override
    public void start() {
        componentNames = Optional.ofNullable(healthEndpoint.healthForPath(GROUP))
                .filter(CompositeHealthDescriptor.class::isInstance)
                .map(CompositeHealthDescriptor.class::cast)
                .map(CompositeHealthDescriptor::getComponents)
                .map(Map::keySet)
                .map(Set::copyOf)
                .orElseGet(Set::of);

        if (componentNames.isEmpty()) {
            return;
        }

        componentNames.forEach(this::registerComponent);
        executorService.submit(() -> refreshComponentHealth());
        taskScheduler.scheduleWithFixedDelay(
                this::refreshComponentHealth,
                platformProperties.getCriticalComponentHealthMetrics().getRefreshInterval());
        running.set(true);
    }

    private void registerComponent(String componentName) {
        AtomicBoolean isUp = new AtomicBoolean(false);
        componentHealth.put(componentName, isUp);
        Gauge.builder(METRIC_NAME, isUp, status -> status.get() ? 1.0 : 0.0)
                .description("Health status of a critical component (1 = UP, 0 = not UP)")
                .tag("component", componentName)
                .register(meterRegistry);
    }

    public void refreshComponentHealth() {
        if (!refreshRunning.compareAndSet(false, true)) {
            return;
        }

        try {
            List<? extends Future<?>> futures = componentNames.stream()
                    .map(componentName -> executorService.submit(() -> refreshComponentHealth(componentName)))
                    .toList();
            futures.forEach(future -> {
                try {
                    future.get();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.warn("Critical component health refresh interrupted", e);
                } catch (Exception e) {
                    log.warn("Critical component health refresh failed", e);
                }
            });
        } finally {
            refreshRunning.set(false);
        }
    }

    private void refreshComponentHealth(String componentName) {
        AtomicBoolean healthReference = componentHealth.get(componentName);

        try {
            HealthDescriptor healthDescriptor = healthEndpoint.healthForPath(componentName);
            healthReference.set(healthDescriptor != null && Status.UP.equals(healthDescriptor.getStatus()));
        } catch (Exception e) {
            healthReference.set(false);
            log.warn("Health check failed for component {}, setting its UP status to false", componentName, e);
        }
    }

    @Override
    public void stop() {
        if (!running.compareAndSet(true, false)) {
            return;
        }

        taskScheduler.close();
        executorService.close();
    }

}
