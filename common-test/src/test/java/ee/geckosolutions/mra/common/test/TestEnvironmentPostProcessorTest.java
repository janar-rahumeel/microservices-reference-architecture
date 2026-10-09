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
package ee.geckosolutions.mra.common.test;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.core.env.MapPropertySource;
import org.springframework.mock.env.MockEnvironment;

class TestEnvironmentPostProcessorTest {

    private static final TestEnvironmentPostProcessor TEST_ENVIRONMENT_POST_PROCESSOR = new TestEnvironmentPostProcessor();

    private final SpringApplication springApplication = new SpringApplication();

    @Test
    void shouldDisableManagementByDefault() {
        // given
        MockEnvironment environment = new MockEnvironment();

        // when
        TEST_ENVIRONMENT_POST_PROCESSOR.postProcessEnvironment(environment, springApplication);

        // then
        assertThat(environment.getProperty("management.server.port", Integer.class)).isEqualTo(-1);
        assertThat(environment.getProperty("management.tracing.export.otlp.enabled", Boolean.class)).isFalse();
        assertThat(environment.getProperty("management.otlp.metrics.export.enabled", Boolean.class)).isFalse();
        assertThat(environment.getProperty("management.logging.export.otlp.enabled", Boolean.class)).isFalse();
        assertThat(environment.getProperty("management.tracing.sampling.probability")).isNull();
    }

    @Test
    void shouldOverrideLowerPrecedenceManagementProperties() {
        // given
        MockEnvironment environment = new MockEnvironment();
        environment.getPropertySources().addLast(new MapPropertySource("defaults", Map.of("management.server.port", 8081)));

        // when
        TEST_ENVIRONMENT_POST_PROCESSOR.postProcessEnvironment(environment, springApplication);

        // then
        assertThat(environment.getProperty("management.server.port", Integer.class)).isEqualTo(-1);
    }

    @Test
    void shouldNotTouchEnvironmentWhenManagementIsEnabled() {
        // given
        MockEnvironment environment = new MockEnvironment().withProperty("application.test.management.enabled", "true");

        // when
        TEST_ENVIRONMENT_POST_PROCESSOR.postProcessEnvironment(environment, springApplication);

        // then
        assertThat(environment.getPropertySources().get("mra-test-defaults")).isNull();
        assertThat(environment.getProperty("management.server.port")).isNull();
    }

}
