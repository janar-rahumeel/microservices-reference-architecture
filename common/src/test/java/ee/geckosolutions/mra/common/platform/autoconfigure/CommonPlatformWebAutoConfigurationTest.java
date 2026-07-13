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

import ee.geckosolutions.mra.common.platform.web.DefaultExceptionHandler;
import ee.geckosolutions.mra.common.platform.web.ErrorResponseV2ExceptionHandler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;

class CommonPlatformWebAutoConfigurationTest {

    private WebApplicationContextRunner webContextRunner;

    @BeforeEach
    void beforeEach() {
        webContextRunner = new WebApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(CommonPlatformWebAutoConfiguration.class));
    }

    @Test
    void shouldCreateExceptionHandlersInServletWebApplication() {
        // given
        webContextRunner

                // when
                .run(context -> {

                    // then
                    assertThat(context).hasSingleBean(DefaultExceptionHandler.class)
                            .hasSingleBean(ErrorResponseV2ExceptionHandler.class);
                });
    }

    @Test
    void shouldBackOffWhenDefaultExceptionHandlerBeanExists() {
        // given
        WebApplicationContextRunner webApplicationContextRunner = webContextRunner
                .withBean("customExceptionHandler", DefaultExceptionHandler.class);

        // when
        webApplicationContextRunner.run(context -> {

            // then
            assertThat(context).hasSingleBean(DefaultExceptionHandler.class)
                    .getBean(DefaultExceptionHandler.class)
                    .isSameAs(context.getBean("customExceptionHandler"));
        });
    }

    @Test
    void shouldBackOffWhenErrorResponseV2ExceptionHandlerBeanExists() {
        // given
        WebApplicationContextRunner webApplicationContextRunner = webContextRunner
                .withBean("customErrorResponseV2ExceptionHandler", ErrorResponseV2ExceptionHandler.class);

        // when
        webApplicationContextRunner.run(context -> {

            // then
            assertThat(context).hasSingleBean(ErrorResponseV2ExceptionHandler.class)
                    .getBean(ErrorResponseV2ExceptionHandler.class)
                    .isSameAs(context.getBean("customErrorResponseV2ExceptionHandler"));
        });
    }

    @Test
    void shouldNotCreateExceptionHandlersInNonWebApplication() {
        // given
        ApplicationContextRunner applicationContextRunner = new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(CommonPlatformWebAutoConfiguration.class));

        // when
        applicationContextRunner.run(context -> {

            // then
            assertThat(context).doesNotHaveBean(DefaultExceptionHandler.class)
                    .doesNotHaveBean(ErrorResponseV2ExceptionHandler.class);
        });
    }

}
