/*
 * SPDX-License-Identifier: NONE
 *
 * Copyright (C) 2026-present Gecko Solutions OÜ
 * All rights reserved.
 *
 * This software is the proprietary and confidential property of Gecko Solutions OÜ.
 * Unauthorized copying, redistribution, or modification of this file, in whole or in part,
 * is strictly prohibited without prior written consent from Gecko Solutions OÜ.
 *
 * For licensing information, contact: licensing@geckosolutions.ee
 */
package ee.geckosolutions.mra.common.platform.observation;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import ee.geckosolutions.mra.common.platform.config.PlatformProperties;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.health.actuate.endpoint.CompositeHealthDescriptor;
import org.springframework.boot.health.actuate.endpoint.HealthDescriptor;
import org.springframework.boot.health.actuate.endpoint.HealthEndpoint;
import org.springframework.boot.health.contributor.Status;
import org.springframework.scheduling.concurrent.SimpleAsyncTaskScheduler;

@Slf4j
@RequiredArgsConstructor
public class CriticalComponentHealthMetrics implements InitializingBean, DisposableBean {

    private static final String GROUP = "critical";
    private static final String METRIC_NAME = "application_critical_component_up";

    private final Map<String, AtomicBoolean> componentHealth = new ConcurrentHashMap<>();
    private final SimpleAsyncTaskScheduler taskScheduler = simpleAsyncTaskScheduler();
    private final ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor();

    private final PlatformProperties platformProperties;
    private final HealthEndpoint healthEndpoint;
    private final MeterRegistry meterRegistry;

    private List<String> componentNames = List.of();

    private static SimpleAsyncTaskScheduler simpleAsyncTaskScheduler() {
        SimpleAsyncTaskScheduler scheduler = new SimpleAsyncTaskScheduler();
        scheduler.setVirtualThreads(true);
        scheduler.setThreadNamePrefix("critical-health-");
        return scheduler;
    }

    @Override
    public void afterPropertiesSet() {
        componentNames = Optional.ofNullable(healthEndpoint.healthForPath(GROUP))
                .filter(CompositeHealthDescriptor.class::isInstance)
                .map(CompositeHealthDescriptor.class::cast)
                .map(CompositeHealthDescriptor::getComponents)
                .map(Map::keySet)
                .map(List::copyOf)
                .orElseGet(List::of);

        if (componentNames.isEmpty()) {
            return;
        }

        componentNames.forEach(this::registerComponent);

        taskScheduler.scheduleWithFixedDelay(
                this::refreshComponentHealth,
                platformProperties.getCriticalComponentHealthMetrics().getRefreshInterval());
    }

    private void registerComponent(String componentName) {
        AtomicBoolean isUp = new AtomicBoolean(false);
        componentHealth.put(componentName, isUp);
        Gauge.builder(METRIC_NAME, isUp, status -> status.get() ? 1.0 : 0.0)
                .description("Health status of a critical component (1 = UP, 0 = not UP)")
                .tag("component", componentName)
                .register(meterRegistry);
    }

    private void refreshComponentHealth() {
        try {
            List<Future<Boolean>> componentHealthFutures = executorService.invokeAll(
                    componentNames.stream()
                            .<Callable<Boolean>> map(componentName -> () -> resolveComponentHealth(componentName))
                            .toList(),
                    platformProperties.getCriticalComponentHealthMetrics().getRefreshTimeout().toMillis(),
                    TimeUnit.MILLISECONDS);

            for (int i = 0; i < componentHealthFutures.size(); i++) {
                String componentName = componentNames.get(i);
                Future<Boolean> componentHealthFuture = componentHealthFutures.get(i);

                if (componentHealthFuture.isCancelled()) {
                    componentHealth.get(componentName).set(false);
                    log.warn("Health check timed out for component {}", componentName);
                    continue;
                }

                try {
                    componentHealth.get(componentName).set(componentHealthFuture.get());
                } catch (ExecutionException e) {
                    componentHealth.get(componentName).set(false);
                    log.warn("Health check failed for component {}", componentName, e.getCause());
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Critical component health refresh interrupted", e);
        }
    }

    private boolean resolveComponentHealth(String componentName) {
        try {
            HealthDescriptor healthDescriptor = healthEndpoint.healthForPath(componentName);
            return healthDescriptor != null && Status.UP.equals(healthDescriptor.getStatus());
        } catch (Exception e) {
            log.warn("Health check failed for component {}", componentName, e);
            return false;
        }
    }

    @Override
    public void destroy() {
        taskScheduler.close();
        executorService.shutdownNow();
    }

}
