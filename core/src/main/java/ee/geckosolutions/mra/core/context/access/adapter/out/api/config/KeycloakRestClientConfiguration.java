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
package ee.geckosolutions.mra.core.context.access.adapter.out.api.config;

import ee.geckosolutions.mra.common.platform.http.HttpClientUtil;
import ee.geckosolutions.mra.core.config.ApplicationProperties;
import ee.geckosolutions.mra.core.context.access.adapter.out.api.KeycloakAdminClientHttpRequestInterceptor;
import ee.geckosolutions.mra.core.context.access.adapter.out.api.KeycloakAdminV1Client;
import ee.geckosolutions.mra.core.context.access.adapter.out.api.KeycloakServiceAccountTokenProvider;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.restclient.autoconfigure.RestClientBuilderConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@RequiredArgsConstructor
public class KeycloakRestClientConfiguration {

    private final ApplicationProperties applicationProperties;
    private final RestClientBuilderConfigurer restClientBuilderConfigurer;

    @Bean
    RestClient.Builder keycloakRestClientBuilder() {
        return restClientBuilderConfigurer.configure(RestClient.builder())
                .apply(HttpClientUtil.configure(applicationProperties.getKeycloak()));
    }

    @Bean
    KeycloakServiceAccountTokenProvider keycloakServiceAccountTokenProvider(RestClient.Builder keycloakRestClientBuilder) {
        return new KeycloakServiceAccountTokenProvider(
                keycloakRestClientBuilder.clone().build(),
                applicationProperties.getKeycloak());
    }

    @Bean
    KeycloakAdminV1Client keycloakAdminV1Client(
            RestClient.Builder keycloakRestClientBuilder,
            KeycloakAdminClientHttpRequestInterceptor requestInterceptor) {
        return HttpClientUtil
                .create(keycloakRestClientBuilder.clone().requestInterceptor(requestInterceptor), KeycloakAdminV1Client.class);
    }

}
