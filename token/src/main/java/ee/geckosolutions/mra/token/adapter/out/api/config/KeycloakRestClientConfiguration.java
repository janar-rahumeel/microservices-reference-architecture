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
package ee.geckosolutions.mra.token.adapter.out.api.config;

import ee.geckosolutions.mra.common.platform.http.HttpClientUtil;
import ee.geckosolutions.mra.token.adapter.out.api.KeycloakOAuth2ErrorV1Exception;
import ee.geckosolutions.mra.token.adapter.out.api.KeycloakV1Client;
import ee.geckosolutions.mra.token.adapter.out.api.dto.OAuth2ErrorV1;
import ee.geckosolutions.mra.token.config.ApplicationProperties;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.restclient.autoconfigure.RestClientBuilderConfigurer;
import org.springframework.boot.restclient.autoconfigure.RestClientSsl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.ObjectMapper;

@Configuration
@RequiredArgsConstructor
public class KeycloakRestClientConfiguration {

    public static final String KEYCLOAK_REST_CLIENT_BUILDER_BEAN_NAME = "keycloakRestClientBuilder";

    private final ApplicationProperties applicationProperties;

    @Bean(KEYCLOAK_REST_CLIENT_BUILDER_BEAN_NAME)
    RestClient.Builder keycloakRestClientBuilder(
            RestClientBuilderConfigurer restClientBuilderConfigurer,
            RestClientSsl restClientSsl,
            ObjectMapper objectMapper) {
        RestClient.Builder builder = restClientBuilderConfigurer.configure(RestClient.builder())
                .apply(HttpClientUtil.configure(applicationProperties.getKeycloak()));
        builder.defaultStatusHandler(HttpStatusCode::isError, (ignoredHttpRequest, clientHttpResponse) -> {
            OAuth2ErrorV1 oAuth2ErrorV1 = objectMapper.readValue(clientHttpResponse.getBody(), OAuth2ErrorV1.class);
            throw new KeycloakOAuth2ErrorV1Exception(clientHttpResponse.getStatusCode(), oAuth2ErrorV1);
        });
        return builder;
    }

    @Bean
    KeycloakV1Client keycloakV1Client(@Qualifier(KEYCLOAK_REST_CLIENT_BUILDER_BEAN_NAME) RestClient.Builder builder) {
        return HttpClientUtil.create(builder, KeycloakV1Client.class);
    }

}
