/*
 * SPDX-License-Identifier: NONE
 *
 * Copyright (C) 2026-present Gecko Solutions OÃœ
 * All rights reserved.
 *
 * This software is the proprietary and confidential property of Gecko Solutions OÃœ.
 * Unauthorized copying, redistribution, or modification of this file, in whole or in part,
 * is strictly prohibited without prior written consent from Gecko Solutions OÃœ.
 *
 * For licensing information, contact: licensing@geckosolutions.ee
 */
package ee.geckosolutions.mra.common.platform.autoconfigure;

import ee.geckosolutions.mra.common.platform.web.DefaultExceptionHandler;
import ee.geckosolutions.mra.common.platform.web.ErrorResponseV2ExceptionHandler;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.restclient.RestClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.zalando.logbook.Strategy;
import org.zalando.logbook.core.BodyOnlyIfStatusAtLeastStrategy;
import org.zalando.logbook.spring.LogbookClientHttpRequestInterceptor;

@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class CommonPlatformAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(DefaultExceptionHandler.class)
    DefaultExceptionHandler defaultExceptionHandler() {
        return new DefaultExceptionHandler();
    }

    @Bean
    @ConditionalOnMissingBean(ErrorResponseV2ExceptionHandler.class)
    ErrorResponseV2ExceptionHandler errorResponseV2ExceptionHandler() {
        return new ErrorResponseV2ExceptionHandler();
    }

    @Bean
    @ConditionalOnProperty(value = "logbook.strategy", havingValue = "body-only-if-status-at-least", matchIfMissing = true)
    @ConditionalOnMissingBean(Strategy.class)
    Strategy bodyOnlyIfStatusAtLeastStrategy(@Value("${logbook.minimum-status:400}") int status) {
        return new BodyOnlyIfStatusAtLeastStrategy(status);
    }

    @Bean
    @ConditionalOnClass({ RestClientCustomizer.class, LogbookClientHttpRequestInterceptor.class })
    RestClientCustomizer restClientCustomizer(LogbookClientHttpRequestInterceptor requestInterceptor) {
        return restClientBuilder -> restClientBuilder.requestInterceptor(requestInterceptor);
    }

}
