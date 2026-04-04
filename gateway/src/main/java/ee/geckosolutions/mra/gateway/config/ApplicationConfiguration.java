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
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatusCode;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(ApplicationProperties.class)
public class ApplicationConfiguration {

    public static final String CORE_SERVICE_REST_CLIENT_BUILDER_BEAN_NAME = "coreServiceRestClientBuilder";

    private final ApplicationProperties applicationProperties;

    @Bean(CORE_SERVICE_REST_CLIENT_BUILDER_BEAN_NAME)
    RestClient.Builder coreServiceRestClientBuilder() {
        HttpServiceProperties httpServiceProperties = applicationProperties.getInternalServices().getCoreService();
        RestClient.Builder builder = HttpClientUtil.customize(RestClient.builder(), httpServiceProperties);
        builder.defaultStatusHandler(HttpStatusCode::isError, (ignoredHttpRequest, ignoredClientHttpResponse) -> {
        });
        return builder;
    }

    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }

}
