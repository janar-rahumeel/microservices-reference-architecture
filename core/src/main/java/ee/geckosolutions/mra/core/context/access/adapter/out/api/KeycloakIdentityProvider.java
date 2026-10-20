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

import java.net.URI;
import java.util.UUID;

import ee.geckosolutions.mra.common.platform.observation.Adapter;
import ee.geckosolutions.mra.common.platform.observation.AdapterDirection;
import ee.geckosolutions.mra.common.platform.observation.AdapterType;
import ee.geckosolutions.mra.common.platform.observation.BoundedContext;
import ee.geckosolutions.mra.core.config.ApplicationProperties;
import ee.geckosolutions.mra.core.context.access.adapter.out.api.dto.KeycloakUserV1Request;
import ee.geckosolutions.mra.core.context.access.application.exception.IdentityProviderException;
import ee.geckosolutions.mra.core.context.access.application.port.IdentityProvider;
import ee.geckosolutions.mra.core.context.access.domain.exception.DuplicateUserException;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientResponseException;

@Adapter(direction = AdapterDirection.OUT, type = AdapterType.REST_CLIENT, boundedContext = BoundedContext.ACCESS)
@Component
@RequiredArgsConstructor
public class KeycloakIdentityProvider implements IdentityProvider {

    private final ApplicationProperties applicationProperties;
    private final KeycloakAdminV1Client keycloakAdminV1Client;

    @Override
    public UUID createUser(NewIdentity identity) {
        ApplicationProperties.Keycloak keycloak = applicationProperties.getKeycloak();
        KeycloakUserV1Request request = KeycloakUserV1Request.builder()
                .username(identity.username())
                .firstName(identity.firstName())
                .lastName(identity.lastName())
                .email(identity.email())
                .enabled(true)
                .emailVerified(false)
                .requiredActions(keycloak.getRequiredActions())
                .build();

        try {
            return extractIdentityId(keycloakAdminV1Client.createUser(request));
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().isSameCodeAs(HttpStatus.CONFLICT)) {
                throw new DuplicateUserException(
                        "Identity already exists by username or e-mail",
                        KeycloakErrors.details("Keycloak rejected a duplicate identity", e));
            }

            throw new IdentityProviderException(KeycloakErrors.details("Failed to create Keycloak user", e));
        }
    }

    @Override
    public void deleteUser(UUID id) {
        try {
            keycloakAdminV1Client.deleteUser(id);
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().isSameCodeAs(HttpStatus.NOT_FOUND)) {
                return;
            }

            throw new IdentityProviderException(KeycloakErrors.details("Failed to delete Keycloak user", e));
        }
    }

    private static UUID extractIdentityId(ResponseEntity<Void> response) {
        URI location = response.getHeaders().getLocation();
        if (location == null) {
            throw new IdentityProviderException("Keycloak did not return the created user location");
        }
        String path = location.getPath();
        try {
            return UUID.fromString(path.substring(path.lastIndexOf('/') + 1));
        } catch (IllegalArgumentException e) {
            throw new IdentityProviderException("Keycloak returned an invalid user location: %s".formatted(location));
        }
    }

}
