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
package ee.geckosolutions.mra.gateway.context.customer.adapter.out.api.config;

import ee.geckosolutions.mra.common.platform.http.HttpClientUtil;
import ee.geckosolutions.mra.gateway.config.ApplicationConfiguration;
import ee.geckosolutions.mra.gateway.context.customer.adapter.out.api.InternalCustomerV1Client;
import ee.geckosolutions.mra.gateway.context.customer.adapter.out.api.InternalCustomerV2Client;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@RequiredArgsConstructor
public class CustomerRestClientConfiguration {

    @Bean
    InternalCustomerV1Client internalCustomerV1Client(
            @Qualifier(ApplicationConfiguration.CORE_SERVICE_REST_CLIENT_BUILDER_BEAN_NAME) RestClient.Builder builder) {
        return HttpClientUtil.create(builder, InternalCustomerV1Client.class);
    }

    @Bean
    InternalCustomerV2Client internalCustomerV2Client(
            @Qualifier(ApplicationConfiguration.CORE_SERVICE_REST_CLIENT_BUILDER_BEAN_NAME) RestClient.Builder builder) {
        return HttpClientUtil.create(builder, InternalCustomerV2Client.class);
    }

}
