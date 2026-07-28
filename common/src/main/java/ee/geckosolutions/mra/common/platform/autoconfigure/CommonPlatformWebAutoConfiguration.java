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

import java.util.function.Predicate;

import ee.geckosolutions.mra.common.platform.web.DefaultExceptionHandler;
import ee.geckosolutions.mra.common.platform.web.ErrorResponseV2ExceptionHandler;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.zalando.logbook.HttpRequest;
import org.zalando.logbook.autoconfigure.LogbookAutoConfiguration;
import org.zalando.logbook.core.Conditions;

@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class CommonPlatformWebAutoConfiguration {

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

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(LogbookAutoConfiguration.class)
    static class LogbookConfiguration {

        @Bean
        Predicate<HttpRequest> requestCondition() {
            return Conditions.requestTo("**/api/v*/**");
        }

    }

}
