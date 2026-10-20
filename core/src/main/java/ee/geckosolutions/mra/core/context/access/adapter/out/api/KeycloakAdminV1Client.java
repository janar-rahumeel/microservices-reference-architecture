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

import java.util.UUID;

import ee.geckosolutions.mra.common.platform.observation.Adapter;
import ee.geckosolutions.mra.common.platform.observation.AdapterDirection;
import ee.geckosolutions.mra.common.platform.observation.AdapterType;
import ee.geckosolutions.mra.common.platform.observation.BoundedContext;
import ee.geckosolutions.mra.core.context.access.adapter.out.api.dto.KeycloakUserV1Request;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.DeleteExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

@Adapter(direction = AdapterDirection.OUT, type = AdapterType.REST_CLIENT, boundedContext = BoundedContext.ACCESS)
@HttpExchange(url = "/admin/realms/mra")
public interface KeycloakAdminV1Client {

    @PostExchange(url = "/users", contentType = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Void> createUser(@RequestBody KeycloakUserV1Request keycloakUserV1Request);

    @DeleteExchange(url = "/users/{id}")
    void deleteUser(@PathVariable UUID id);

}
