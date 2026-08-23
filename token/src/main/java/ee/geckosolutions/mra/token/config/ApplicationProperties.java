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
package ee.geckosolutions.mra.token.config;

import java.util.HashSet;
import java.util.Set;

import ee.geckosolutions.mra.common.platform.http.HttpServiceProperties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(value = "application")
public class ApplicationProperties {

    private final Security security = new Security();
    private final Keycloak keycloak = new Keycloak();

    @Getter
    @Setter
    public static class Security {

        private final Cors cors = new Cors();
        private String realmName;

        @Getter
        @Setter
        public static class Cors {

            private Set<String> allowedOrigins = new HashSet<>();

        }

    }

    @Getter
    @Setter
    public static class Keycloak implements HttpServiceProperties {

        private String baseUrl;

    }

}
