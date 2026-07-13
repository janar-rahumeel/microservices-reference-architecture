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

import ee.geckosolutions.mra.common.platform.observation.CommonObservationAspect;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.jackson.autoconfigure.JacksonAutoConfiguration;
import org.springframework.boot.micrometer.observation.autoconfigure.ObservationAutoConfiguration;
import org.springframework.boot.restclient.RestClientCustomizer;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.zalando.logbook.Strategy;
import org.zalando.logbook.autoconfigure.LogbookAutoConfiguration;
import org.zalando.logbook.core.BodyOnlyIfStatusAtLeastStrategy;
import org.zalando.logbook.core.WithoutBodyStrategy;
import org.zalando.logbook.servlet.AsyncCompletionDecorator;
import org.zalando.logbook.servlet.MicrometerAsyncCompletionDecorator;

class CommonPlatformAutoConfigurationTest {

    private ApplicationContextRunner contextRunner;

    @BeforeEach
    void beforeEach() {
        contextRunner = new ApplicationContextRunner().withConfiguration(
                AutoConfigurations.of(CommonPlatformAutoConfiguration.class, ObservationAutoConfiguration.class));
    }

    @Test
    void shouldCreateCommonObservationAspectByDefault() {
        // given
        ApplicationContextRunner applicationContextRunner = contextRunner.withClassLoader(
                new FilteredClassLoader(
                        "org.zalando.logbook",
                        "org.springframework.boot.restclient",
                        "org.springframework.amqp.support.converter",
                        "tools.jackson.databind.json"));

        // when
        applicationContextRunner.run(context -> {

            // then
            assertThat(context).hasSingleBean(CommonObservationAspect.class);
        });
    }

    @Test
    void shouldNotCreateCommonObservationAspectWhenObservationIsDisabled() {
        // given
        ApplicationContextRunner applicationContextRunner = contextRunner
                .withClassLoader(
                        new FilteredClassLoader(
                                "org.zalando.logbook",
                                "org.springframework.boot.restclient",
                                "org.springframework.amqp.support.converter",
                                "tools.jackson.databind.json"))
                .withPropertyValues("application.common.observation.enabled=false");

        // when
        applicationContextRunner.run(context -> {

            // then
            assertThat(context).doesNotHaveBean(CommonObservationAspect.class);
        });
    }

    @Test
    void shouldCreateLogbookStrategyByDefault() {
        // given
        ApplicationContextRunner applicationContextRunner = contextRunner
                .withConfiguration(AutoConfigurations.of(LogbookAutoConfiguration.class))
                .withClassLoader(
                        new FilteredClassLoader(
                                "org.springframework.boot.restclient",
                                "org.springframework.amqp.support.converter"));

        // when
        applicationContextRunner.run(context -> {

            // then
            assertThat(context).hasSingleBean(Strategy.class)
                    .getBean(Strategy.class)
                    .isInstanceOf(BodyOnlyIfStatusAtLeastStrategy.class);
        });
    }

    @Test
    void shouldNotCreateLogbookStrategyWhenStrategyPropertyDoesNotMatch() {
        // given
        ApplicationContextRunner applicationContextRunner = contextRunner
                .withConfiguration(AutoConfigurations.of(LogbookAutoConfiguration.class))
                .withClassLoader(
                        new FilteredClassLoader(
                                "org.springframework.boot.restclient",
                                "org.springframework.amqp.support.converter"))
                .withPropertyValues("logbook.strategy=without-body");

        // when
        applicationContextRunner.run(context -> {

            // then
            assertThat(context).hasSingleBean(Strategy.class).getBean(Strategy.class).isInstanceOf(WithoutBodyStrategy.class);
        });
    }

    @Test
    void shouldBackOffWhenLogbookStrategyBeanExists() {
        // given
        ApplicationContextRunner applicationContextRunner = contextRunner
                .withConfiguration(AutoConfigurations.of(LogbookAutoConfiguration.class))
                .withClassLoader(
                        new FilteredClassLoader(
                                "org.springframework.boot.restclient",
                                "org.springframework.amqp.support.converter"))
                .withBean(Strategy.class, () -> new Strategy() {
                });

        // when
        applicationContextRunner.run(context -> {

            // then
            assertThat(context).hasSingleBean(Strategy.class).doesNotHaveBean("commonStrategy");
        });
    }

    @Test
    void shouldCreateAsyncCompletionDecoratorByDefault() {
        // given
        ApplicationContextRunner applicationContextRunner = contextRunner
                .withConfiguration(AutoConfigurations.of(LogbookAutoConfiguration.class))
                .withClassLoader(
                        new FilteredClassLoader(
                                "org.springframework.boot.restclient",
                                "org.springframework.amqp.support.converter"));

        // when
        applicationContextRunner.run(context -> {

            // then
            assertThat(context).hasSingleBean(AsyncCompletionDecorator.class)
                    .getBean(AsyncCompletionDecorator.class)
                    .isInstanceOf(MicrometerAsyncCompletionDecorator.class);
        });
    }

    @Test
    void shouldCreateLogbookFilterByDefault() {
        // given
        ApplicationContextRunner applicationContextRunner = contextRunner
                .withConfiguration(AutoConfigurations.of(LogbookAutoConfiguration.class))
                .withClassLoader(
                        new FilteredClassLoader(
                                "org.springframework.boot.restclient",
                                "org.springframework.amqp.support.converter"));

        // when
        applicationContextRunner.run(context -> {

            // then
            assertThat(context).hasBean("logbookFilter");
        });
    }

    @Test
    void shouldNotCreateLogbookFilterBeansWhenLogbookFilterIsDisabled() {
        // given
        ApplicationContextRunner applicationContextRunner = contextRunner
                .withClassLoader(
                        new FilteredClassLoader(
                                "org.springframework.boot.restclient",
                                "org.springframework.amqp.support.converter",
                                "tools.jackson.databind.json"))
                .withPropertyValues("logbook.filter.enabled=false");

        // when
        applicationContextRunner.run(context -> {

            // then
            assertThat(context).doesNotHaveBean("logbookFilter").doesNotHaveBean(AsyncCompletionDecorator.class);
        });
    }

    @Test
    void shouldBackOffWhenLogbookFilterBeanExists() {
        // given
        ApplicationContextRunner applicationContextRunner = contextRunner
                .withClassLoader(
                        new FilteredClassLoader(
                                "org.springframework.boot.restclient",
                                "org.springframework.amqp.support.converter",
                                "tools.jackson.databind.json"))
                .withBean("logbookFilter", String.class);

        // when
        applicationContextRunner.run(context -> {

            // then
            assertThat(context).hasBean("logbookFilter").getBean("logbookFilter").isInstanceOf(String.class);
        });
    }

    @Test
    void shouldCreateRestClientCustomizerWhenLogbookInterceptorExists() {
        // given
        ApplicationContextRunner applicationContextRunner = contextRunner
                .withConfiguration(AutoConfigurations.of(LogbookAutoConfiguration.class))
                .withClassLoader(new FilteredClassLoader("org.springframework.amqp.support.converter"));

        // when
        applicationContextRunner.run(context -> {

            // then
            assertThat(context).hasSingleBean(RestClientCustomizer.class);
        });
    }

    @Test
    void shouldNotCreateRestClientCustomizerWhenLogbookSpringIsNotPresent() {
        // given
        ApplicationContextRunner applicationContextRunner = contextRunner
                .withConfiguration(AutoConfigurations.of(JacksonAutoConfiguration.class))
                .withClassLoader(new FilteredClassLoader("org.zalando.logbook"));

        // when
        applicationContextRunner.run(context -> {

            // then
            assertThat(context).doesNotHaveBean(RestClientCustomizer.class);
        });
    }

    @Test
    void shouldCreateRabbitJsonMessageConverter() {
        // given
        ApplicationContextRunner applicationContextRunner = contextRunner
                .withConfiguration(AutoConfigurations.of(JacksonAutoConfiguration.class))
                .withClassLoader(new FilteredClassLoader("org.zalando.logbook", "org.springframework.boot.restclient"));

        // when
        applicationContextRunner.run(context -> {

            // then
            assertThat(context).hasSingleBean(JacksonJsonMessageConverter.class);
        });
    }

    @Test
    void shouldNotCreateRabbitJsonMessageConverterWhenJsonMapperIsNotPresent() {
        // given
        ApplicationContextRunner applicationContextRunner = contextRunner.withClassLoader(
                new FilteredClassLoader(
                        "org.zalando.logbook",
                        "org.springframework.boot.restclient",
                        "tools.jackson.databind.json"));

        // when
        applicationContextRunner.run(context -> {

            // then
            assertThat(context).doesNotHaveBean(JacksonJsonMessageConverter.class);
        });
    }

}
