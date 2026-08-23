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
package ee.geckosolutions.mra.token.adapter.out.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.jackson.Jacksonized;
import org.jspecify.annotations.Nullable;

@Getter
@Builder
@Jacksonized
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class KeycloakOpenIdConnectTokenV1Response {

    @Setter
    @JsonProperty(value = "access_token", required = true)
    private String accessToken;

    @JsonProperty(value = "expires_in", required = true)
    private final long expiresIn;

    @JsonProperty(value = "token_type", required = true)
    private final String tokenType;

    @Nullable
    @JsonProperty("id_token")
    private final String idToken;

    @JsonProperty(value = "not-before-policy", required = true)
    private final long notBeforePolicy;

    @Nullable
    @JsonProperty("session_state")
    private final String sessionState;

    @JsonProperty(value = "scope", required = true)
    private final String scope;

    @Setter
    @Nullable
    @JsonProperty("refresh_token")
    private String refreshToken;

    @JsonProperty(value = "refresh_expires_in", required = true)
    private final long refreshExpiresIn;

}
