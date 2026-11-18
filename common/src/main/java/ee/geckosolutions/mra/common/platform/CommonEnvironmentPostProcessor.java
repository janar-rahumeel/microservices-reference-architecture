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
package ee.geckosolutions.mra.common.platform;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.EnvironmentPostProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.util.ClassUtils;

/**
 * @see <a href="https://docs.spring.io/spring-boot/appendix/application-properties/index.html">Spring Boot application
 *      properties</a>
 */
public class CommonEnvironmentPostProcessor implements EnvironmentPostProcessor {

    static final String PROPERTY_SOURCE_NAME = "mra-defaults";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment configurableEnvironment, SpringApplication springApplication) {
        Map<String, Object> defaultProperties = new HashMap<>();
        defaultProperties.put("spring.task.execution.propagate-context", true);
        defaultProperties.put("spring.threads.virtual.enabled", true);
        defaultProperties.put("logging.structured.format.console", "ecs");
        defaultProperties.put("logging.structured.json.exclude", "tags");
        defaultProperties.put("logging.structured.json.context.include", false);

        if (managementDefaultsEnabled(configurableEnvironment)) {
            defaultProperties.putAll(managementDefaultProperties());
        }

        if (ClassUtils.isPresent("tools.jackson.databind.ObjectMapper", springApplication.getClassLoader())) {
            defaultProperties.putAll(jacksonDefaultProperties());
        }

        if (ClassUtils.isPresent("org.springframework.data.jpa.repository.JpaRepository", springApplication.getClassLoader())) {
            defaultProperties.putAll(persistenceDefaultProperties());
        }

        if (configurableEnvironment.containsProperty("spring.datasource.url")
                || configurableEnvironment.containsProperty("spring.datasource.jndi-name")) {
            defaultProperties.putAll(datasourceDefaultProperties());
        }

        if (ClassUtils.isPresent(
                "net.ttddyy.observation.boot.autoconfigure.DataSourceObservationAutoConfiguration",
                springApplication.getClassLoader())) {
            defaultProperties.putAll(datasourceObservationDefaultProperties());
        }

        if (ClassUtils.isPresent(
                "org.springframework.boot.amqp.autoconfigure.RabbitAutoConfiguration",
                springApplication.getClassLoader())) {
            defaultProperties.putAll(rabbitMqDefaultProperties());
        }

        if (ClassUtils
                .isPresent("org.zalando.logbook.autoconfigure.LogbookAutoConfiguration", springApplication.getClassLoader())) {
            defaultProperties.put("logging.level.org.zalando.logbook", "TRACE");
            defaultProperties.put("logbook.ecs.custom-namespace-prefix", "mra");
        }

        if (!defaultProperties.isEmpty()) {
            configurableEnvironment.getPropertySources()
                    .addLast(new MapPropertySource(PROPERTY_SOURCE_NAME, defaultProperties));
        }
    }

    private static boolean managementDefaultsEnabled(ConfigurableEnvironment configurableEnvironment) {
        return configurableEnvironment.getProperty("management.server.port", Integer.class, 0) > -1;
    }

    private static Map<String, Object> managementDefaultProperties() {
        return Map.of(
                "management.tracing.sampling.probability",
                1.0,
                "management.otlp.metrics.export.enabled",
                false,
                "management.logging.export.otlp.enabled",
                false,
                "management.opentelemetry.tracing.export.otlp.transport",
                "grpc",
                "management.metrics.tags.service_name",
                "${spring.application.name}",
                "management.endpoints.web.base-path",
                "/internal/actuator",
                "management.endpoints.web.exposure.include",
                "health,prometheus,info",
                "management.server.ssl.enabled",
                false,
                "management.server.port",
                8081);
    }

    private static Map<String, Object> jacksonDefaultProperties() {
        return Map.of(
                "spring.jackson.default-property-inclusion",
                "non_null",
                "spring.jackson.deserialization.fail-on-unknown-properties",
                true);
    }

    private static Map<String, Object> persistenceDefaultProperties() {
        return Map.of(
                "spring.jpa.open-in-view",
                false,
                "spring.jpa.hibernate.ddl-auto",
                "none",
                "spring.data.jpa.repositories.bootstrap-mode",
                "deferred");
    }

    private static Map<String, Object> datasourceDefaultProperties() {
        return Map.of("spring.datasource.connection-fetch", "lazy");
    }

    private static Map<String, Object> datasourceObservationDefaultProperties() {
        return Map.of("jdbc.hikari.enabled", false, "jdbc.opentelemetry.metrics.enabled", false);
    }

    private static Map<String, Object> rabbitMqDefaultProperties() {
        return Map.of(
                "spring.rabbitmq.template.observation-enabled",
                true,
                "spring.rabbitmq.listener.simple.observation-enabled",
                true);
    }

}
