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
package ee.geckosolutions.mra.common.platform.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import ee.geckosolutions.mra.common.platform.config.PlatformProperties;
import ee.geckosolutions.mra.common.platform.observation.CriticalComponentHealthMetrics;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.health.actuate.endpoint.HealthEndpoint;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class CommonPlatformHealthAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(CommonPlatformHealthAutoConfiguration.class));

    @Test
    void shouldCreateCriticalComponentHealthMetricsWhenCriticalGroupIsConfigured() {
        // given
        ApplicationContextRunner applicationContextRunner = contextRunner
                .withPropertyValues("management.endpoint.health.group.critical.include=db,rabbit")
                .withBean(PlatformProperties.class, PlatformProperties::new)
                .withBean(HealthEndpoint.class, () -> mock(HealthEndpoint.class))
                .withBean(MeterRegistry.class, SimpleMeterRegistry::new);

        // when
        applicationContextRunner.run(context -> {

            // then
            assertThat(context).hasSingleBean(CriticalComponentHealthMetrics.class);
        });
    }

    @Test
    void shouldNotCreateCriticalComponentHealthMetricsWhenCriticalGroupIsNotConfigured() {
        // given
        // when
        contextRunner.run(context -> {

            // then
            assertThat(context).doesNotHaveBean(CriticalComponentHealthMetrics.class);
        });
    }

    @Test
    void shouldNotCreateCriticalComponentHealthMetricsWhenCriticalGroupIsEmpty() {
        // given
        ApplicationContextRunner applicationContextRunner = contextRunner
                .withPropertyValues("management.endpoint.health.group.critical.include=");

        // when
        applicationContextRunner.run(context -> {

            // then
            assertThat(context).doesNotHaveBean(CriticalComponentHealthMetrics.class);
        });
    }

}
