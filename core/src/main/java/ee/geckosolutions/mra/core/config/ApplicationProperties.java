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
package ee.geckosolutions.mra.core.config;

import java.util.ArrayList;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import ee.geckosolutions.mra.common.platform.http.ServiceProperties;
import ee.geckosolutions.mra.common.platform.web.security.ServerCredential;
import ee.geckosolutions.mra.common.platform.web.security.Sha256Hash;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@ConfigurationProperties(value = "application")
public class ApplicationProperties {

    @Valid
    private final Security security = new Security();
    private final Keycloak keycloak = new Keycloak();

    @Getter
    @Setter
    public static class Security {

        @NotEmpty
        private final List<@Valid Credential> serverCredentials = new ArrayList<>();

        @Getter
        @Setter
        public static class Credential implements ServerCredential {

            @NotBlank
            private String username;

            @NotNull
            private Sha256Hash password;

        }

    }

    @Getter
    @Setter
    public static class Keycloak implements ServiceProperties {

        private final ServiceAccount serviceAccount = new ServiceAccount();

        private String baseUrl;
        private List<String> requiredActions = new ArrayList<>(List.of("UPDATE_PASSWORD"));

        @Getter
        @Setter
        public static class ServiceAccount {

            private String clientId;
            private String clientSecret;

        }

    }

}
