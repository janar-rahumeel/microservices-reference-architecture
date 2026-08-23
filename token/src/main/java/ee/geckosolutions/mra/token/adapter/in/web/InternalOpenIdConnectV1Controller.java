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

import java.time.Duration;

import ee.geckosolutions.mra.common.platform.observation.Adapter;
import ee.geckosolutions.mra.common.platform.observation.AdapterDirection;
import ee.geckosolutions.mra.common.platform.observation.AdapterType;
import ee.geckosolutions.mra.token.adapter.out.api.KeycloakOAuth2ErrorV1Exception;
import ee.geckosolutions.mra.token.application.OpenIdConnectTokenService;
import ee.geckosolutions.mra.token.application.dto.TokenExchangeResult;
import ee.geckosolutions.mra.token.application.port.AccessTokenRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Adapter(direction = AdapterDirection.IN, type = AdapterType.REST)
@RestController
@RequestMapping("/internal/api/v1/openid-connect")
@RequiredArgsConstructor
@OAuth2ErrorResponseV1Api
public class InternalOpenIdConnectV1Controller implements InternalOpenIdConnectV1Api {

    static final String KEYCLOAK_SESSION_COOKIE_NAME = "KEYCLOAK_SESSION";
    static final String SESSION_COOKIE_NAME = "MRA_SESSION";

    private static final String SESSION_COOKIE_DOMAIN = "mra.local";

    private final OpenIdConnectTokenService openIdConnectTokenService;
    private final AccessTokenRepository accessTokenRepository;

    @Override
    @PostMapping(
            path = "/token",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> token(
            @RequestParam MultiValueMap<String, String> parameters,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) @Nullable String authorization,
            @CookieValue(value = KEYCLOAK_SESSION_COOKIE_NAME, required = false) @Nullable String keycloakSessionKey,
            @CookieValue(value = SESSION_COOKIE_NAME, required = false) @Nullable String sessionId) {
        try {
            TokenExchangeResult tokenExchangeResult = openIdConnectTokenService
                    .exchange(parameters, authorization, keycloakSessionKey, sessionId);
            ResponseEntity.BodyBuilder bodyBuilder = ResponseEntity.ok();
            tokenExchangeResult.accessSessionId()
                    .ifPresent(
                            id -> bodyBuilder.header(
                                    HttpHeaders.SET_COOKIE,
                                    createSessionCookie(
                                            id,
                                            Duration.ofSeconds(tokenExchangeResult.tokenResponse().getExpiresIn()))
                                            .toString()));
            return bodyBuilder.body(tokenExchangeResult.tokenResponse());
        } catch (KeycloakOAuth2ErrorV1Exception e) {
            return ResponseEntity.status(e.getHttpStatusCode()).body(e.getOAuth2ErrorV1());
        }
    }

    private static ResponseCookie createSessionCookie(String sessionId, Duration maxAge) {
        return ResponseCookie.from(SESSION_COOKIE_NAME, sessionId)
                .httpOnly(true)
                .secure(true)
                .sameSite("Lax")
                .domain(SESSION_COOKIE_DOMAIN)
                .path("/api/")
                .maxAge(maxAge)
                .build();
    }

    @Override
    @RequestMapping(path = "/session-token")
    public ResponseEntity<Void> sessionToken(
            @CookieValue(value = SESSION_COOKIE_NAME, required = false) @Nullable String sessionId) {
        if (sessionId == null) {
            return ResponseEntity.ok().build();
        }

        return accessTokenRepository.get(sessionId)
                .map(
                        accessToken -> ResponseEntity.ok()
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                                .<Void> build())
                .orElseGet(() -> ResponseEntity.ok().build());
    }

}
