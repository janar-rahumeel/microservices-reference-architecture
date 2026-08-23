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
package ee.geckosolutions.mra.token.application;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

import ee.geckosolutions.mra.token.adapter.in.web.OAuth2ErrorCodes;
import ee.geckosolutions.mra.token.adapter.in.web.OAuth2GrantTypes;
import ee.geckosolutions.mra.token.adapter.out.api.KeycloakOAuth2ErrorV1Exception;
import ee.geckosolutions.mra.token.adapter.out.api.KeycloakV1Client;
import ee.geckosolutions.mra.token.adapter.out.api.dto.KeycloakOpenIdConnectTokenV1Response;
import ee.geckosolutions.mra.token.application.dto.TokenExchangeResult;
import ee.geckosolutions.mra.token.application.exception.InvalidRefreshTokenException;
import ee.geckosolutions.mra.token.application.port.AccessTokenRepository;
import ee.geckosolutions.mra.token.application.port.RefreshTokenRepository;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;
import org.springframework.http.ResponseEntity;
import org.springframework.integration.redis.util.RedisLockRegistry;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@Service
@RequiredArgsConstructor
public class OpenIdConnectTokenService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final KeycloakV1Client keycloakV1Client;
    private final RedisLockRegistry redisLockRegistry;
    private final RefreshTokenRepository refreshTokenRepository;
    private final AccessTokenRepository accessTokenRepository;

    public TokenExchangeResult exchange(
            MultiValueMap<String, String> parameters,
            @Nullable String authorization,
            @Nullable String keycloakSessionKey,
            @Nullable String sessionId) throws KeycloakOAuth2ErrorV1Exception {
        String grantType = parameters.getFirst("grant_type");

        if (StringUtils.isNotBlank(grantType)) {
            if (authorizationCodeGrant(grantType)) {
                return authorizationCode(parameters, keycloakSessionKey, sessionId);
            }

            if (clientCredentialsGrant(grantType)) {
                return clientCredentials(parameters, authorization);
            }

            if (refreshTokenGrant(grantType)) {
                return refreshToken(parameters, authorization, keycloakSessionKey, sessionId);
            }
        }

        return exchangeWithKeycloak(parameters, authorization, null, null, false);
    }

    private static boolean authorizationCodeGrant(String grantType) {
        return OAuth2GrantTypes.AUTHORIZATION_CODE.equalsIgnoreCase(grantType);
    }

    private TokenExchangeResult authorizationCode(
            MultiValueMap<String, String> parameters,
            @Nullable String keycloakSessionKey,
            @Nullable String sessionId) throws KeycloakOAuth2ErrorV1Exception {
        return exchangeWithKeycloak(parameters, null, keycloakSessionKey, sessionId, true);
    }

    private static boolean clientCredentialsGrant(String grantType) {
        return OAuth2GrantTypes.CLIENT_CREDENTIALS.equalsIgnoreCase(grantType);
    }

    private TokenExchangeResult clientCredentials(MultiValueMap<String, String> parameters, @Nullable String authorization)
            throws KeycloakOAuth2ErrorV1Exception {
        return exchangeWithKeycloak(parameters, authorization, null, null, false);
    }

    private static boolean refreshTokenGrant(String grantType) {
        return OAuth2GrantTypes.REFRESH_TOKEN.equalsIgnoreCase(grantType);
    }

    private TokenExchangeResult refreshToken(
            MultiValueMap<String, String> parameters,
            @Nullable String authorization,
            @Nullable String keycloakSessionKey,
            @Nullable String sessionId) throws KeycloakOAuth2ErrorV1Exception {
        if (keycloakSessionKey != null) {
            return refreshTokenWithKeycloakSessionKey(parameters, keycloakSessionKey, sessionId);
        }

        if (authorization != null) {
            return refreshTokenWithAuthorization(parameters, authorization);
        }

        throw new InvalidRefreshTokenException(
                "Either Keycloak session key or authorization is required for refresh token grant");
    }

    private TokenExchangeResult refreshTokenWithKeycloakSessionKey(
            MultiValueMap<String, String> parameters,
            String keycloakSessionKey,
            @Nullable String sessionId) throws KeycloakOAuth2ErrorV1Exception {
        Lock lock = redisLockRegistry.obtain("oauth2:refresh-token-lock:" + keycloakSessionKey);

        try {
            if (!lock.tryLock(10, TimeUnit.SECONDS)) {
                throw new IllegalStateException(String.format("Could not acquire refresh token lock (%s)", keycloakSessionKey));
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Request has been interrupted", e);
        }

        try {
            Optional<String> refreshToken = refreshTokenRepository.get(keycloakSessionKey);

            if (refreshToken.isEmpty()) {
                throw new InvalidRefreshTokenException(
                        String.format("No refresh token found by Keycloak session key (%s)", keycloakSessionKey));
            }

            MultiValueMap<String, String> keycloakParameters = new LinkedMultiValueMap<>(parameters);
            keycloakParameters.set("refresh_token", refreshToken.get());
            return exchangeWithKeycloak(keycloakParameters, null, keycloakSessionKey, sessionId, true);
        } catch (KeycloakOAuth2ErrorV1Exception e) {
            if (OAuth2ErrorCodes.INVALID_GRANT.equals(e.getOAuth2ErrorV1().getError())) {
                refreshTokenRepository.delete(keycloakSessionKey);

                if (sessionId != null) {
                    accessTokenRepository.delete(sessionId);
                }
            }

            throw e;
        } finally {
            lock.unlock();
        }
    }

    private TokenExchangeResult refreshTokenWithAuthorization(MultiValueMap<String, String> parameters, String authorization)
            throws KeycloakOAuth2ErrorV1Exception {
        return exchangeWithKeycloak(parameters, authorization, null, null, false);
    }

    private TokenExchangeResult exchangeWithKeycloak(
            MultiValueMap<String, String> parameters,
            @Nullable String authorization,
            @Nullable String keycloakSessionKey,
            @Nullable String sessionId,
            boolean spaFlow) throws KeycloakOAuth2ErrorV1Exception {
        ResponseEntity<KeycloakOpenIdConnectTokenV1Response> responseEntity = keycloakV1Client.token(authorization, parameters);
        KeycloakOpenIdConnectTokenV1Response keycloakOpenIdConnectTokenV1Response = Objects
                .requireNonNull(responseEntity.getBody(), "Keycloak response is null");

        if (!spaFlow) {
            return new TokenExchangeResult(keycloakOpenIdConnectTokenV1Response, Optional.empty());
        }

        if (keycloakSessionKey != null && keycloakOpenIdConnectTokenV1Response.getRefreshToken() != null) {
            refreshTokenRepository.save(
                    keycloakSessionKey,
                    keycloakOpenIdConnectTokenV1Response.getRefreshToken(),
                    Duration.ofSeconds(keycloakOpenIdConnectTokenV1Response.getRefreshExpiresIn()));
            keycloakOpenIdConnectTokenV1Response.setRefreshToken(generateOpaqueValue());
        }

        String resolvedAccessSessionId = Optional.ofNullable(sessionId).orElseGet(() -> UUID.randomUUID().toString());
        accessTokenRepository.save(
                resolvedAccessSessionId,
                keycloakOpenIdConnectTokenV1Response.getAccessToken(),
                Duration.ofSeconds(keycloakOpenIdConnectTokenV1Response.getExpiresIn()));
        keycloakOpenIdConnectTokenV1Response
                .setAccessToken(maskAccessToken(keycloakOpenIdConnectTokenV1Response.getAccessToken()));
        return new TokenExchangeResult(keycloakOpenIdConnectTokenV1Response, Optional.of(resolvedAccessSessionId));
    }

    private static String maskAccessToken(String accessToken) {
        String[] segments = accessToken.split("\\.");
        return segments[0] + "." + segments[1] + "." + generateOpaqueValue();
    }

    private static String generateOpaqueValue() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

}
