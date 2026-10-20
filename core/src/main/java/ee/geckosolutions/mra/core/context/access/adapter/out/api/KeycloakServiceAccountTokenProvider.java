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
package ee.geckosolutions.mra.core.context.access.adapter.out.api;

import ee.geckosolutions.mra.core.config.ApplicationProperties;
import ee.geckosolutions.mra.core.context.access.adapter.out.api.dto.KeycloakTokenV1Response;
import ee.geckosolutions.mra.core.context.access.application.exception.IdentityProviderException;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@RequiredArgsConstructor
public class KeycloakServiceAccountTokenProvider {

    private final RestClient restClient;
    private final ApplicationProperties.Keycloak keycloak;

    String getAccessToken() {
        MultiValueMap<String, String> multiValueMap = new LinkedMultiValueMap<>();
        multiValueMap.add("grant_type", "client_credentials");
        multiValueMap.add("client_id", keycloak.getServiceAccount().getClientId());
        multiValueMap.add("client_secret", keycloak.getServiceAccount().getClientSecret());

        try {
            KeycloakTokenV1Response response = restClient.post()
                    .uri("/realms/mra/protocol/openid-connect/token")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(multiValueMap)
                    .retrieve()
                    .requiredBody(KeycloakTokenV1Response.class);

            return response.accessToken();
        } catch (RestClientResponseException e) {
            throw new IdentityProviderException(KeycloakErrors.details("Failed to obtain Keycloak service account token", e));
        }
    }

}
