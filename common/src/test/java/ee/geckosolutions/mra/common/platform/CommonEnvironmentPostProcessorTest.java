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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.mock.env.MockEnvironment;

class CommonEnvironmentPostProcessorTest {

    private static final CommonEnvironmentPostProcessor COMMON_ENVIRONMENT_POST_PROCESSOR = new CommonEnvironmentPostProcessor();

    private SpringApplication springApplication;

    @BeforeEach
    void beforeEach() {
        springApplication = Mockito.spy(new SpringApplication(Object.class));
    }

    @Test
    void shouldAddCommonProperties() {
        // given
        MockEnvironment environment = new MockEnvironment();

        // when
        COMMON_ENVIRONMENT_POST_PROCESSOR.postProcessEnvironment(environment, springApplication);

        // then
        MapPropertySource mapPropertySource = getMraSource(environment);
        assertThat(mapPropertySource).isNotNull();
        assertThat(mapPropertySource.getProperty("spring.task.execution.propagate-context")).isEqualTo(true);
        assertThat(mapPropertySource.getProperty("management.tracing.sampling.probability")).isEqualTo(1.0);
        assertThat(mapPropertySource.getProperty("management.otlp.metrics.export.enabled")).isEqualTo(false);
        assertThat(mapPropertySource.getProperty("management.logging.export.otlp.enabled")).isEqualTo(false);
        assertThat(mapPropertySource.getProperty("management.opentelemetry.tracing.export.otlp.transport")).isEqualTo("grpc");
        assertThat(mapPropertySource.getProperty("management.metrics.tags.application"))
                .isEqualTo("${spring.application.name}");
        assertThat(mapPropertySource.getProperty("management.endpoints.web.base-path")).isEqualTo("/internal/actuator");
        assertThat(mapPropertySource.getProperty("management.endpoints.web.exposure.include"))
                .isEqualTo("health,prometheus,info");
        assertThat(mapPropertySource.getProperty("management.server.ssl.enabled")).isEqualTo(false);
        assertThat(mapPropertySource.getProperty("management.server.port")).isEqualTo(8081);
        assertThat(mapPropertySource.getProperty("spring.jackson.default-property-inclusion")).isEqualTo("non_null");
        assertThat(mapPropertySource.getProperty("spring.jackson.deserialization.fail-on-unknown-properties")).isEqualTo(true);
        assertThat(mapPropertySource.getProperty("spring.jpa.open-in-view")).isEqualTo(false);
        assertThat(mapPropertySource.getProperty("spring.jpa.hibernate.ddl-auto")).isEqualTo("none");
        assertThat(mapPropertySource.getProperty("spring.data.jpa.repositories.bootstrap-mode")).isEqualTo("deferred");
        assertThat(mapPropertySource.getProperty("spring.datasource.connection-fetch")).isNull();
        assertThat(mapPropertySource.getProperty("jdbc.hikari.enabled")).isEqualTo(false);
        assertThat(mapPropertySource.getProperty("spring.rabbitmq.template.observation-enabled")).isEqualTo(true);
        assertThat(mapPropertySource.getProperty("spring.rabbitmq.listener.simple.observation-enabled")).isEqualTo(true);
    }

    @Test
    void shouldNotAddManagementDefaultsWhenTurnedOff() {
        // given
        MockEnvironment environment = new MockEnvironment().withProperty("management.server.port", "-1");
        given(springApplication.getClassLoader()).willReturn(new FilteredClassLoader("tools.jackson.databind"));

        // when
        COMMON_ENVIRONMENT_POST_PROCESSOR.postProcessEnvironment(environment, springApplication);

        // then
        MapPropertySource mapPropertySource = (MapPropertySource) environment.getPropertySources().get("mra-defaults");
        assertThat(mapPropertySource).isNotNull();
        assertThat(mapPropertySource.getProperty("management.tracing.sampling.probability")).isNull();
        assertThat(mapPropertySource.getProperty("management.otlp.metrics.export.enabled")).isNull();
        assertThat(mapPropertySource.getProperty("management.logging.export.otlp.enabled")).isNull();
        assertThat(mapPropertySource.getProperty("management.opentelemetry.tracing.export.otlp.transport")).isNull();
        assertThat(mapPropertySource.getProperty("management.metrics.tags.application")).isNull();
        assertThat(mapPropertySource.getProperty("management.endpoints.web.base-path")).isNull();
        assertThat(mapPropertySource.getProperty("management.endpoints.web.exposure.include")).isNull();
        assertThat(mapPropertySource.getProperty("management.server.ssl.enabled")).isNull();
        assertThat(mapPropertySource.getProperty("management.server.port")).isNull();
    }

    @Test
    void shouldNotAddJacksonDefaultsWhenJackson3IsNotPresent() {
        // given
        MockEnvironment environment = new MockEnvironment();
        given(springApplication.getClassLoader()).willReturn(new FilteredClassLoader("tools.jackson.databind"));

        // when
        COMMON_ENVIRONMENT_POST_PROCESSOR.postProcessEnvironment(environment, springApplication);

        // then
        MapPropertySource mapPropertySource = (MapPropertySource) environment.getPropertySources().get("mra-defaults");
        assertThat(mapPropertySource).isNotNull();
        assertThat(mapPropertySource.getProperty("spring.jackson.default-property-inclusion")).isNull();
        assertThat(mapPropertySource.getProperty("spring.jackson.deserialization.fail-on-unknown-properties")).isNull();
    }

    @Test
    void shouldNotAddJpaDefaultsWhenSpringDataJpaIsNotPresent() {
        // given
        MockEnvironment environment = new MockEnvironment();
        given(springApplication.getClassLoader())
                .willReturn(new FilteredClassLoader("org.springframework.data.jpa.repository"));

        // when
        COMMON_ENVIRONMENT_POST_PROCESSOR.postProcessEnvironment(environment, springApplication);

        // then
        MapPropertySource mapPropertySource = getMraSource(environment);
        assertThat(mapPropertySource.getProperty("spring.jpa.open-in-view")).isNull();
        assertThat(mapPropertySource.getProperty("spring.jpa.hibernate.ddl-auto")).isNull();
        assertThat(mapPropertySource.getProperty("spring.data.jpa.repositories.bootstrap-mode")).isNull();
    }

    @Test
    void shouldAddDatasourceDefaultsWhenDatasourceUrlConfigured() {
        // given
        MockEnvironment environment = new MockEnvironment()
                .withProperty("spring.datasource.url", "jdbc:postgresql://localhost/test");

        // when
        COMMON_ENVIRONMENT_POST_PROCESSOR.postProcessEnvironment(environment, springApplication);

        // then
        MapPropertySource mapPropertySource = getMraSource(environment);
        assertThat(mapPropertySource.getProperty("spring.datasource.connection-fetch")).isEqualTo("lazy");
    }

    @Test
    void shouldAddDatasourceDefaultsWhenDatasourceJndiNameConfigured() {
        // given
        MockEnvironment environment = new MockEnvironment()
                .withProperty("spring.datasource.jndi-name", "java:comp/env/jdbc/test");

        // when
        COMMON_ENVIRONMENT_POST_PROCESSOR.postProcessEnvironment(environment, springApplication);

        // then
        MapPropertySource mapPropertySource = getMraSource(environment);
        assertThat(mapPropertySource.getProperty("spring.datasource.connection-fetch")).isEqualTo("lazy");
    }

    @Test
    void shouldNotAddDatasourceDefaultsWhenDatasourceIsNotConfigured() {
        // given
        MockEnvironment environment = new MockEnvironment();

        // when
        COMMON_ENVIRONMENT_POST_PROCESSOR.postProcessEnvironment(environment, springApplication);

        // then
        MapPropertySource mapPropertySource = getMraSource(environment);
        assertThat(mapPropertySource.getProperty("spring.datasource.connection-fetch")).isNull();
    }

    @Test
    void shouldNotAddDatasourceObservationDefaultsWhenDatasourceObservationIsNotPresent() {
        // given
        MockEnvironment environment = new MockEnvironment();
        given(springApplication.getClassLoader())
                .willReturn(new FilteredClassLoader("net.ttddyy.observation.boot.autoconfigure"));

        // when
        COMMON_ENVIRONMENT_POST_PROCESSOR.postProcessEnvironment(environment, springApplication);

        // then
        MapPropertySource mapPropertySource = getMraSource(environment);
        assertThat(mapPropertySource.getProperty("jdbc.hikari.enabled")).isNull();
    }

    @Test
    void shouldNotAddRabbitMqDefaultsWhenRabbitAutoConfigurationIsNotPresent() {
        // given
        MockEnvironment environment = new MockEnvironment();
        given(springApplication.getClassLoader())
                .willReturn(new FilteredClassLoader("org.springframework.boot.amqp.autoconfigure"));

        // when
        COMMON_ENVIRONMENT_POST_PROCESSOR.postProcessEnvironment(environment, springApplication);

        // then
        MapPropertySource mapPropertySource = getMraSource(environment);
        assertThat(mapPropertySource.getProperty("spring.rabbitmq.template.observation-enabled")).isNull();
        assertThat(mapPropertySource.getProperty("spring.rabbitmq.listener.simple.observation-enabled")).isNull();
    }

    private static MapPropertySource getMraSource(ConfigurableEnvironment configurableEnvironment) {
        return (MapPropertySource) configurableEnvironment.getPropertySources()
                .get(CommonEnvironmentPostProcessor.PROPERTY_SOURCE_NAME);
    }

}
