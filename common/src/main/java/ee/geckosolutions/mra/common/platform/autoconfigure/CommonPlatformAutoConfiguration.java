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

import java.util.Map;

import ee.geckosolutions.mra.common.contract.customer.messaging.dto.CustomerCreatedEventV1;
import ee.geckosolutions.mra.common.platform.config.PlatformProperties;
import ee.geckosolutions.mra.common.platform.observation.CommonObservationAspect;
import ee.geckosolutions.mra.common.platform.observation.EcsLoggingDomainEventListener;

import io.micrometer.observation.ObservationRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.support.converter.DefaultJacksonJavaTypeMapper;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.restclient.RestClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.zalando.logbook.Strategy;
import org.zalando.logbook.StructuredHttpLogFormatter;
import org.zalando.logbook.autoconfigure.LogbookProperties;
import org.zalando.logbook.core.BodyOnlyIfStatusAtLeastStrategy;
import org.zalando.logbook.ecs.EcsStructuredHttpLogFormatter;
import org.zalando.logbook.ecs.HttpStatusCodeBasedEcsSink;
import org.zalando.logbook.spring.LogbookClientHttpRequestInterceptor;
import tools.jackson.databind.json.JsonMapper;

@Slf4j
@AutoConfiguration(beforeName = "org.zalando.logbook.ecs.autoconfigure.LogbookEcsAutoConfiguration")
@EnableConfigurationProperties(PlatformProperties.class)
public class CommonPlatformAutoConfiguration {

    @Bean
    EcsLoggingDomainEventListener ecsLoggingDomainEventListener() {
        return new EcsLoggingDomainEventListener();
    }

    @Bean
    @ConditionalOnProperty(value = "application.common.observation.enabled", havingValue = "true", matchIfMissing = true)
    CommonObservationAspect commonObservationAspect(ObservationRegistry observationRegistry) {
        return new CommonObservationAspect(observationRegistry);
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(Strategy.class)
    static class LogbookConfiguration {

        @Bean
        StructuredHttpLogFormatter ecsStructuredHttpLogFormatter(
                ObjectProvider<LogbookProperties> logbookPropertiesObjectProvider) {
            return new EcsStructuredHttpLogFormatter(logbookPropertiesObjectProvider);
        }

        @Bean
        HttpStatusCodeBasedEcsSink httpStatusCodeBasedEcsSink(StructuredHttpLogFormatter structuredHttpLogFormatter) {
            return new HttpStatusCodeBasedEcsSink(structuredHttpLogFormatter);
        }

        @Bean
        @ConditionalOnProperty(value = "logbook.strategy", havingValue = "body-only-if-status-at-least", matchIfMissing = true)
        @ConditionalOnMissingBean(Strategy.class)
        Strategy commonStrategy(@Value("${logbook.minimum-status:400}") int status) {
            return new BodyOnlyIfStatusAtLeastStrategy(status);
        }

    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass({ RestClientCustomizer.class, LogbookClientHttpRequestInterceptor.class })
    static class RestClientConfiguration {

        @Bean
        RestClientCustomizer commonRestClientCustomizer(
                LogbookClientHttpRequestInterceptor logbookClientHttpRequestInterceptor) {
            return restClientBuilder -> restClientBuilder.requestInterceptor(logbookClientHttpRequestInterceptor);
        }

    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass({ JacksonJsonMessageConverter.class, JsonMapper.class })
    static class RabbitConfiguration {

        @Bean
        JacksonJsonMessageConverter jacksonJsonMessageConverter(JsonMapper jsonMapper) {
            JacksonJsonMessageConverter jacksonJsonMessageConverter = new JacksonJsonMessageConverter(jsonMapper);
            DefaultJacksonJavaTypeMapper defaultJacksonJavaTypeMapper = new DefaultJacksonJavaTypeMapper();
            defaultJacksonJavaTypeMapper.setIdClassMapping(Map.of("CUSTOMER_CREATED_EVENT_V1", CustomerCreatedEventV1.class));
            jacksonJsonMessageConverter.setJavaTypeMapper(defaultJacksonJavaTypeMapper);
            return jacksonJsonMessageConverter;
        }

    }

}
