/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright (C) 2026-present Gecko Solutions OÜ
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ee.geckosolutions.mra.token.test;

import java.util.HashMap;
import java.util.Map;

import ee.geckosolutions.mra.token.adapter.out.api.config.KeycloakRestClientConfiguration;

import com.redis.testcontainers.RedisContainer;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

@TestConfiguration(proxyBeanMethods = false)
class IntegrationTestConfiguration {

    public static final String KEYCLOAK_MOCK_REST_SERVICE_SERVER_BEAN_NAME = "keycloakMockRestServiceServer";

    private static final Map<String, String> REST_CLIENT_BUILDER_BEAN_NAMES = new HashMap<>();

    static {
        REST_CLIENT_BUILDER_BEAN_NAMES.put(
                KeycloakRestClientConfiguration.KEYCLOAK_REST_CLIENT_BUILDER_BEAN_NAME,
                KEYCLOAK_MOCK_REST_SERVICE_SERVER_BEAN_NAME);
    }

    @Bean
    static BeanPostProcessor restClientBuilderBinder(ConfigurableListableBeanFactory configurableListableBeanFactory) {
        return new BeanPostProcessor() {

            @Override
            public Object postProcessAfterInitialization(Object bean, String name) {
                if (REST_CLIENT_BUILDER_BEAN_NAMES.containsKey(name) && bean instanceof RestClient.Builder restClientBuilder) {
                    MockRestServiceServer mockRestServiceServer = MockRestServiceServer.bindTo(restClientBuilder)
                            .ignoreExpectOrder(true)
                            .build();
                    String mockRestServiceServerBeanName = REST_CLIENT_BUILDER_BEAN_NAMES.get(name);
                    if (!configurableListableBeanFactory.containsSingleton(mockRestServiceServerBeanName)) {
                        configurableListableBeanFactory.registerSingleton(mockRestServiceServerBeanName, mockRestServiceServer);
                    }
                }

                return bean;
            }

        };
    }

    @Bean
    @ServiceConnection
    RedisContainer redisContainer() {
        return new RedisContainer("redis:8.10.1-alpine");
    }

}
