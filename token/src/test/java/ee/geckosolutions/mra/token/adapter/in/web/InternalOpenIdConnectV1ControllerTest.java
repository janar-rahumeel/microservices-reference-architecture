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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.net.URI;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ee.geckosolutions.mra.token.application.port.AccessTokenRepository;
import ee.geckosolutions.mra.token.application.port.RefreshTokenRepository;
import ee.geckosolutions.mra.token.test.AbstractWebIntegrationTest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import tools.jackson.core.type.TypeReference;

class InternalOpenIdConnectV1ControllerTest extends AbstractWebIntegrationTest {

    private static final URI KEYCLOAK_TOKEN_URI = URI.create("http://keycloak.test/realms/mra/protocol/openid-connect/token");

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private AccessTokenRepository accessTokenRepository;

    @Test
    void testThatAuthorizationCodeGrantIsSuccessful() {
        // given
        String realAccessToken = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0LXVzZXIiLCJhdWQiOiJlc3RmZWVkIiwiZXhwIjoxNzYwMDAwMDAwfQ.X7Yq8mJ2KpL9vN4sT6wR1xC5bH3fG8dQ2zA0eI9uM";

        keycloakMockRestServiceServer.expect(requestTo(KEYCLOAK_TOKEN_URI))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(
                        content().formDataContains(
                                Map.of(
                                        "grant_type",
                                        OAuth2GrantTypes.AUTHORIZATION_CODE,
                                        "client_id",
                                        "test_client_id",
                                        "code",
                                        "SplxlOBeZQQYbYS6WxSbIA",
                                        "redirect_uri",
                                        "http://ui.test/callback")))
                .andRespond(withSuccess("""
                        {
                          "access_token": "%s",
                          "expires_in": 300,
                          "refresh_token": "keycloak-refresh-token",
                          "refresh_expires_in": 300,
                          "token_type": "Bearer"
                        }
                        """.formatted(realAccessToken), MediaType.APPLICATION_JSON));

        // when
        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
        parameters.add("grant_type", OAuth2GrantTypes.AUTHORIZATION_CODE);
        parameters.add("client_id", "test_client_id");
        parameters.add("code", "SplxlOBeZQQYbYS6WxSbIA");
        parameters.add("redirect_uri", "http://ui.test/callback");

        String keycloakSessionKey = UUID.randomUUID().toString();
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.COOKIE, "KEYCLOAK_SESSION=" + keycloakSessionKey);

        ResponseEntity<String> response = testRestTemplate.exchange(
                "/internal/api/v1/openid-connect/token",
                HttpMethod.POST,
                new HttpEntity<>(parameters, headers),
                String.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        String setCookieValue = response.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
        assertThat(setCookieValue).isNotNull()
                .contains("Max-Age=300")
                .contains("Domain=mra.local")
                .contains("Path=/")
                .contains("Secure")
                .contains("HttpOnly")
                .contains("SameSite=Lax");

        Pattern pattern = Pattern
                .compile("MRA_SESSION=([0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12})");
        Matcher matcher = pattern.matcher(setCookieValue);
        assertThat(matcher.find()).isTrue();

        String sessionId = matcher.group(1);
        assertThat(refreshTokenRepository.get(keycloakSessionKey)).isPresent();
        assertThat(accessTokenRepository.get(sessionId)).contains(realAccessToken);

        Map<String, String> body = objectMapper.readValue(response.getBody(), new TypeReference<>() {
        });
        assertThat(body.get("access_token")).isNotEqualTo(realAccessToken).startsWith(headerAndPayload(realAccessToken) + ".");
        assertThat(body.get("refresh_token")).isNotEqualTo("keycloak-refresh-token");

        keycloakMockRestServiceServer.verify();
    }

    @Test
    void testThatRefreshTokenGrantIsSuccessful() {
        // given
        String keycloakSessionKey = UUID.randomUUID().toString();
        refreshTokenRepository.save(keycloakSessionKey, "keycloak-refresh-token", Duration.ofSeconds(1));

        String sessionId = UUID.randomUUID().toString();

        String realAccessToken = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0LXVzZXIiLCJhdWQiOiJlc3RmZWVkIiwiZXhwIjoxNzYwMDAwMDAwfQ.X7Yq8mJ2KpL9vN4sT6wR1xC5bH3fG8dQ2zA0eI9uM";
        keycloakMockRestServiceServer.expect(requestTo(KEYCLOAK_TOKEN_URI))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(
                        content().formDataContains(
                                Map.of(
                                        "grant_type",
                                        OAuth2GrantTypes.REFRESH_TOKEN,
                                        "client_id",
                                        "test_client_id",
                                        "refresh_token",
                                        "keycloak-refresh-token")))
                .andRespond(withSuccess("""
                        {
                          "access_token": "%s",
                          "expires_in": 300,
                          "refresh_token": "new-keycloak-refresh-token",
                          "refresh_expires_in": 300,
                          "token_type": "Bearer"
                        }
                        """.formatted(realAccessToken), MediaType.APPLICATION_JSON));

        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
        parameters.add("grant_type", OAuth2GrantTypes.REFRESH_TOKEN);
        parameters.add("client_id", "test_client_id");
        parameters.add("refresh_token", "fake-refresh-token");

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.COOKIE, "KEYCLOAK_SESSION=" + keycloakSessionKey + "; MRA_SESSION=" + sessionId);

        // when
        ResponseEntity<String> response = testRestTemplate.exchange(
                "/internal/api/v1/openid-connect/token",
                HttpMethod.POST,
                new HttpEntity<>(parameters, headers),
                String.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        String setCookieValue = response.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
        assertThat(setCookieValue).isNotNull().contains("MRA_SESSION=" + sessionId).contains("Max-Age=300");

        Map<String, String> body = objectMapper.readValue(response.getBody(), new TypeReference<>() {
        });
        assertThat(body.get("access_token")).isNotEqualTo(realAccessToken).startsWith(headerAndPayload(realAccessToken) + ".");
        assertThat(body.get("refresh_token")).isNotEqualTo("new-keycloak-refresh-token");

        assertThat(refreshTokenRepository.get(keycloakSessionKey)).isPresent().contains("new-keycloak-refresh-token");
        assertThat(accessTokenRepository.get(sessionId)).contains(realAccessToken);

        keycloakMockRestServiceServer.verify();
    }

    @Test
    void testThatSessionTokenResolvesRealAccessTokenForKnownSession() {
        // given
        String sessionId = UUID.randomUUID().toString();
        String realAccessToken = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0LXVzZXIiLCJhdWQiOiJlc3RmZWVkIiwiZXhwIjoxNzYwMDAwMDAwfQ.X7Yq8mJ2KpL9vN4sT6wR1xC5bH3fG8dQ2zA0eI9uM";
        accessTokenRepository.save(sessionId, realAccessToken, Duration.ofMinutes(5));

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.COOKIE, "MRA_SESSION=" + sessionId);

        // when
        ResponseEntity<Void> response = testRestTemplate.exchange(
                "/internal/api/v1/openid-connect/session-token",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                Void.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getFirst(HttpHeaders.AUTHORIZATION)).isEqualTo("Bearer " + realAccessToken);
    }

    @Test
    void testThatSessionTokenIsEmptyForUnknownSession() {
        // when
        ResponseEntity<Void> response = testRestTemplate
                .getForEntity("/internal/api/v1/openid-connect/session-token", Void.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().get(HttpHeaders.AUTHORIZATION)).isNull();
    }

    private static String headerAndPayload(String jwt) {
        String[] segments = jwt.split("\\.");
        return segments[0] + "." + segments[1];
    }

    @Test
    void testThatClientCredentialsGrantIsSuccessful() {
        // given
        keycloakMockRestServiceServer.expect(requestTo(KEYCLOAK_TOKEN_URI))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(
                        content().formDataContains(
                                Map.of(
                                        "grant_type",
                                        OAuth2GrantTypes.CLIENT_CREDENTIALS,
                                        "client_id",
                                        "test_client_id",
                                        "client_secret",
                                        "test_client_secret")))
                .andRespond(withSuccess("""
                        {
                          "access_token": "access-token",
                          "token_type": "Bearer",
                          "expires_in": 300
                        }
                        """, MediaType.APPLICATION_JSON));

        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
        parameters.add("grant_type", OAuth2GrantTypes.CLIENT_CREDENTIALS);
        parameters.add("client_id", "test_client_id");
        parameters.add("client_secret", "test_client_secret");

        // when
        ResponseEntity<String> response = testRestTemplate
                .postForEntity("/internal/api/v1/openid-connect/token", parameters, String.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().get(HttpHeaders.SET_COOKIE)).isNull();

        Map<String, String> body = objectMapper.readValue(response.getBody(), new TypeReference<>() {
        });
        assertThat(body.get("access_token")).isEqualTo("access-token");
        assertThat(body.get("refresh_token")).isNull();

        keycloakMockRestServiceServer.verify();
    }

}
