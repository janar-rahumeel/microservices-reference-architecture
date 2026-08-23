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
package ee.geckosolutions.mra.token.adapter.in.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;

@Tag(name = "OpenID Connect API", description = "OpenID Connect operations")
public interface InternalOpenIdConnectV1Api {

    @Operation(
            summary = "Exchange authorization code for tokens",
            description = "Exchanges an OAuth2 authorization code for an access token")
    ResponseEntity<?> token(
            MultiValueMap<String, String> parameters,
            String authorization,
            String keycloakSessionKey,
            String accessSessionId);

    @Operation(
            summary = "Resolve the real access token for a session",
            description = "Used by the reverse proxy to exchange the access session cookie for the real access token")
    ResponseEntity<Void> sessionToken(String accessSessionId);

}
