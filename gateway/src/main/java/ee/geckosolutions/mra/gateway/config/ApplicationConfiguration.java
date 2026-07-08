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
package ee.geckosolutions.mra.gateway.config;

import java.time.Clock;

import ee.geckosolutions.mra.common.platform.http.HttpClientUtil;
import ee.geckosolutions.mra.common.platform.http.HttpServiceProperties;
import ee.geckosolutions.mra.gateway.adapter.in.web.EndpointDeprecationHandler;

import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.restclient.autoconfigure.RestClientBuilderConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.client.RestClient;
import org.springframework.web.servlet.config.annotation.ApiVersionConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(ApplicationProperties.class)
public class ApplicationConfiguration {

    public static final String CORE_SERVICE_REST_CLIENT_BUILDER_BEAN_NAME = "coreServiceRestClientBuilder";

    private final ApplicationProperties applicationProperties;

    @Bean
    EndpointDeprecationHandler endpointDeprecationHandler() {
        return EndpointDeprecationHandler.of(applicationProperties);
    }

    @Bean
    OpenApiCustomizer deprecationOpenApiCustomizer(ApplicationProperties applicationProperties) {
        return openApi -> applicationProperties.getApi().getDeprecatedEndpoints().forEach(deprecatedEndpoint -> {
            PathItem pathItem = openApi.getPaths().get(deprecatedEndpoint.pathTemplate());

            if (pathItem != null) {
                deprecatedEndpoint.methods().forEach(method -> {
                    Operation operation = pathItem.readOperationsMap().get(PathItem.HttpMethod.valueOf(method.name()));
                    if (operation != null) {
                        operation.setDeprecated(true);

                        if (deprecatedEndpoint.successorLink() != null) {
                            String description = (operation.getDescription() != null ? operation.getDescription() + ". " : "")
                                    + "Use " + deprecatedEndpoint.successorLink() + " instead";
                            operation.setDescription(description);
                        }
                    }
                });
            }
        });
    }

    @Bean(CORE_SERVICE_REST_CLIENT_BUILDER_BEAN_NAME)
    RestClient.Builder coreServiceRestClientBuilder(RestClientBuilderConfigurer restClientBuilderConfigurer) {
        HttpServiceProperties httpServiceProperties = applicationProperties.getInternalServices().getCoreService();
        RestClient.Builder builder = HttpClientUtil.customize(RestClient.builder(), httpServiceProperties);
        builder.defaultStatusHandler(HttpStatusCode::isError, (ignoredHttpRequest, ignoredClientHttpResponse) -> {
        });
        return restClientBuilderConfigurer.configure(builder);
    }

    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }

    @Configuration(proxyBeanMethods = false)
    @RequiredArgsConstructor
    static class WebConfiguration implements WebMvcConfigurer {

        private final EndpointDeprecationHandler deprecationHandler;

        @Override
        public void configureApiVersioning(ApiVersionConfigurer configurer) {
            configurer.setDeprecationHandler(deprecationHandler).setSupportedVersionPredicate(ignored -> true);
        }

    }

}
