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
package ee.geckosolutions.mra.common.platform.config;

import java.time.Duration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "application.platform")
public class PlatformProperties {

    private final CriticalComponentHealthMetrics criticalComponentHealthMetrics = new CriticalComponentHealthMetrics();

    @Getter
    @Setter
    public static class CriticalComponentHealthMetrics {

        private Duration refreshTimeout = Duration.ofSeconds(30);
        private Duration refreshInterval = Duration.ofSeconds(10);

    }

}
