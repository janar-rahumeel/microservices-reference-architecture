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

import java.util.Map;

import org.springframework.boot.EnvironmentPostProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

public class TestEnvironmentPostProcessor implements EnvironmentPostProcessor {

    static final String PROPERTY_SOURCE_NAME = "mra-test-defaults";
    static final String MANAGEMENT_ENABLED_PROPERTY = "application.test.management.enabled";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment configurableEnvironment, SpringApplication springApplication) {
        if (configurableEnvironment.getProperty(MANAGEMENT_ENABLED_PROPERTY, Boolean.class, false)) {
            return;
        }

        configurableEnvironment.getPropertySources()
                .addFirst(
                        new MapPropertySource(
                                PROPERTY_SOURCE_NAME,
                                Map.of(
                                        "management.server.port",
                                        -1,
                                        "management.tracing.export.otlp.enabled",
                                        false,
                                        "management.otlp.metrics.export.enabled",
                                        false,
                                        "management.logging.export.otlp.enabled",
                                        false)));
    }

}
