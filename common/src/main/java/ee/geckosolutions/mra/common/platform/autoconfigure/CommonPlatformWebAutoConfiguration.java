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

import ee.geckosolutions.mra.common.platform.observation.ecs.EcsLoggingContextContributor;
import ee.geckosolutions.mra.common.platform.observation.ecs.EcsLoggingContextFilter;
import ee.geckosolutions.mra.common.platform.observation.ecs.UserEcsLoggingContextContributor;
import ee.geckosolutions.mra.common.platform.web.DefaultExceptionHandler;
import ee.geckosolutions.mra.common.platform.web.ErrorResponseV2ExceptionHandler;

import io.micrometer.context.ContextRegistry;
import io.micrometer.context.ContextSnapshot;
import io.micrometer.context.ContextSnapshotFactory;
import io.micrometer.context.integration.Slf4jThreadLocalAccessor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.SecurityFilterChain;
import org.zalando.logbook.HttpRequest;
import org.zalando.logbook.autoconfigure.LogbookAutoConfiguration;
import org.zalando.logbook.core.Conditions;
import org.zalando.logbook.servlet.AsyncOnCompleteListenerWrapper;

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

    @Bean
    EcsLoggingContextFilter ecsLoggingContextFilter(ObjectProvider<EcsLoggingContextContributor> objectProvider) {
        return new EcsLoggingContextFilter(objectProvider.orderedStream().toList());
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(LogbookAutoConfiguration.class)
    static class LogbookConfiguration {

        @Bean
        Predicate<HttpRequest> requestCondition() {
            return Conditions.requestTo("**/api/v*/**");
        }

        @Bean
        ContextSnapshotFactory contextSnapshotFactory() {
            ContextRegistry contextRegistry = ContextRegistry.getInstance()
                    .registerThreadLocalAccessor(new Slf4jThreadLocalAccessor());
            return ContextSnapshotFactory.builder().contextRegistry(contextRegistry).build();
        }

        @Bean
        AsyncOnCompleteListenerWrapper asyncOnCompleteListenerWrapper(ContextSnapshotFactory contextSnapshotFactory) {
            return asyncOnCompleteListener -> {
                ContextSnapshot contextSnapshot = contextSnapshotFactory.captureAll();
                return asyncEvent -> {
                    try (ContextSnapshot.Scope ignored = contextSnapshot.setThreadLocals()) {
                        asyncOnCompleteListener.onComplete(asyncEvent);
                    }
                };
            };
        }

    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(SecurityFilterChain.class) // TODO change when every MS requires security
    static class SecurityConfiguration {

        @Bean
        @ConditionalOnBean(SecurityFilterChain.class)
        EcsLoggingContextContributor userEcsLoggingContextContributor() {
            return new UserEcsLoggingContextContributor();
        }

    }

}
