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

import io.micrometer.registry.otlp.OtlpMeterRegistry;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;

@SpringBootTest
class TestEnvironmentPostProcessorIntegrationTest {

    @Autowired
    private Environment environment;

    @Autowired
    private ObjectProvider<SpanExporter> spanExporters;

    @Autowired
    private ObjectProvider<OtlpMeterRegistry> otlpMeterRegistries;

    @Test
    void shouldDisableManagementServerAndTelemetryExportWhenCommonTestIsOnClasspath() {
        // given
        // when
        // then
        assertThat(environment.getProperty("management.server.port", Integer.class)).isEqualTo(-1);
        assertThat(spanExporters.stream()).isEmpty();
        assertThat(otlpMeterRegistries.stream()).isEmpty();
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    static class TestApplication {
    }

}
