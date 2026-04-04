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

import java.time.Instant;
import java.time.Period;

import ee.geckosolutions.mra.common.platform.http.HttpServiceProperties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(value = "application")
public class ApplicationProperties {

    private final InternalServices internalServices = new InternalServices();
    private final Customer customer = new Customer();
    private Period apiDeprecationSunsetPeriod = Period.parse("P6M");

    @Getter
    @Setter
    public static class InternalServices {

        private final CoreService coreService = new CoreService();

        @Getter
        @Setter
        public static class CoreService implements HttpServiceProperties {

            private String baseUrl;

        }

    }

    @Getter
    @Setter
    public static class Customer {

        private Instant v1ApiDeprecatedSince;

    }

}
