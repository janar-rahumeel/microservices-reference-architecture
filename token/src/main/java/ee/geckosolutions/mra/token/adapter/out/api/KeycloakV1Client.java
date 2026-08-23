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
package ee.geckosolutions.mra.token.adapter.out.api;

import ee.geckosolutions.mra.common.platform.observation.Adapter;
import ee.geckosolutions.mra.common.platform.observation.AdapterDirection;
import ee.geckosolutions.mra.common.platform.observation.AdapterType;
import ee.geckosolutions.mra.token.adapter.out.api.dto.KeycloakOpenIdConnectTokenV1Response;

import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

@Adapter(direction = AdapterDirection.OUT, type = AdapterType.REST_CLIENT)
@HttpExchange(url = "/realms/mra")
public interface KeycloakV1Client {

    @PostExchange(
            url = "/protocol/openid-connect/token",
            contentType = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            accept = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<KeycloakOpenIdConnectTokenV1Response> token(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) @Nullable String authorization,
            @RequestBody MultiValueMap<String, String> parameters) throws KeycloakOAuth2ErrorV1Exception;

}
