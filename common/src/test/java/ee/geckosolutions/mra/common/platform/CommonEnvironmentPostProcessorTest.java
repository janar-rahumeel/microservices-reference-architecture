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
import org.springframework.core.env.StandardEnvironment;

class CommonEnvironmentPostProcessorTest {

    private static final CommonEnvironmentPostProcessor COMMON_ENVIRONMENT_POST_PROCESSOR = new CommonEnvironmentPostProcessor();

    private SpringApplication springApplication;

    @BeforeEach
    void beforeEach() {
        springApplication = Mockito.spy(new SpringApplication(Object.class));
    }

    @Test
    void shouldAddJacksonDefaultsWhenJackson3IsPresent() {
        // given
        ConfigurableEnvironment configurableEnvironment = new StandardEnvironment();

        // when
        COMMON_ENVIRONMENT_POST_PROCESSOR.postProcessEnvironment(configurableEnvironment, springApplication);

        // then
        MapPropertySource mapPropertySource = (MapPropertySource) configurableEnvironment.getPropertySources()
                .get("mra-defaults");
        assertThat(mapPropertySource).isNotNull();
        assertThat(mapPropertySource.getProperty("spring.jackson.default-property-inclusion")).isEqualTo("non_null");
        assertThat(mapPropertySource.getProperty("spring.jackson.deserialization.fail-on-unknown-properties")).isEqualTo(true);
    }

    @Test
    void shouldNotAddJacksonDefaultsWhenJackson3IsNotPresent() {
        // given
        ConfigurableEnvironment configurableEnvironment = new StandardEnvironment();
        given(springApplication.getClassLoader()).willReturn(new FilteredClassLoader("tools.jackson.databind"));

        // when
        COMMON_ENVIRONMENT_POST_PROCESSOR.postProcessEnvironment(configurableEnvironment, springApplication);

        // then
        MapPropertySource mapPropertySource = (MapPropertySource) configurableEnvironment.getPropertySources()
                .get("mra-defaults");
        assertThat(mapPropertySource).isNotNull();
        assertThat(mapPropertySource.getProperty("spring.jackson.default-property-inclusion")).isNull();
        assertThat(mapPropertySource.getProperty("spring.jackson.deserialization.fail-on-unknown-properties")).isNull();
    }

    @Test
    void shouldAddJpaDefaultsWhenSpringDataJpaIsPresent() {
        // given
        ConfigurableEnvironment configurableEnvironment = new StandardEnvironment();

        // when
        COMMON_ENVIRONMENT_POST_PROCESSOR.postProcessEnvironment(configurableEnvironment, springApplication);

        // then
        MapPropertySource mapPropertySource = (MapPropertySource) configurableEnvironment.getPropertySources()
                .get("mra-defaults");
        assertThat(mapPropertySource.getProperty("spring.jpa.open-in-view")).isEqualTo(false);
        assertThat(mapPropertySource.getProperty("spring.jpa.hibernate.ddl-auto")).isEqualTo("none");
        assertThat(mapPropertySource.getProperty("spring.data.jpa.repositories.bootstrap-mode")).isEqualTo("deferred");
    }

    @Test
    void shouldNotAddJpaDefaultsWhenSpringDataJpaIsNotPresent() {
        // given
        ConfigurableEnvironment configurableEnvironment = new StandardEnvironment();
        given(springApplication.getClassLoader())
                .willReturn(new FilteredClassLoader("org.springframework.data.jpa.repository"));

        // when
        COMMON_ENVIRONMENT_POST_PROCESSOR.postProcessEnvironment(configurableEnvironment, springApplication);

        // then
        MapPropertySource mapPropertySource = getMraSource(configurableEnvironment);
        assertThat(mapPropertySource.getProperty("spring.jpa.open-in-view")).isNull();
        assertThat(mapPropertySource.getProperty("spring.jpa.hibernate.ddl-auto")).isNull();
        assertThat(mapPropertySource.getProperty("spring.data.jpa.repositories.bootstrap-mode")).isNull();
    }

    @Test
    void shouldAddDatasourceDefaultsWhenDatasourceUrlConfigured() {
        // given
        ConfigurableEnvironment configurableEnvironment = new StandardEnvironment();
        configurableEnvironment.getPropertySources()
                .addFirst(
                        new MapPropertySource(
                                "test",
                                java.util.Map.of("spring.datasource.url", "jdbc:postgresql://localhost/test")));

        // when
        COMMON_ENVIRONMENT_POST_PROCESSOR.postProcessEnvironment(configurableEnvironment, springApplication);

        // then
        MapPropertySource mapPropertySource = getMraSource(configurableEnvironment);
        assertThat(mapPropertySource.getProperty("spring.datasource.connection-fetch")).isEqualTo("lazy");
    }

    private static MapPropertySource getMraSource(ConfigurableEnvironment configurableEnvironment) {
        return (MapPropertySource) configurableEnvironment.getPropertySources().get("mra-defaults");
    }

}
