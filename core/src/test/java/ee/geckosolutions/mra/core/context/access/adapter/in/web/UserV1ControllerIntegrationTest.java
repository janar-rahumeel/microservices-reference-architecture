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
package ee.geckosolutions.mra.core.context.access.adapter.in.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.net.URI;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import ee.geckosolutions.mra.common.contract.access.web.dto.NewUserV1Request;
import ee.geckosolutions.mra.common.contract.access.web.dto.UserV1Response;
import ee.geckosolutions.mra.common.platform.web.dto.ErrorResponseV2;
import ee.geckosolutions.mra.core.context.access.adapter.out.persistence.entity.PermissionEntity;
import ee.geckosolutions.mra.core.context.access.adapter.out.persistence.entity.RoleEntity;
import ee.geckosolutions.mra.core.context.access.adapter.out.persistence.entity.UserEntity;
import ee.geckosolutions.mra.core.context.access.adapter.out.persistence.repository.PermissionJpaRepository;
import ee.geckosolutions.mra.core.context.access.adapter.out.persistence.repository.RoleJpaRepository;
import ee.geckosolutions.mra.core.context.access.adapter.out.persistence.repository.UserJpaRepository;
import ee.geckosolutions.mra.core.test.AbstractWebIntegrationTest;
import ee.geckosolutions.mra.core.test.TestUtil;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.restclient.test.MockServerRestClientCustomizer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.ResponseActions;

class UserV1ControllerIntegrationTest extends AbstractWebIntegrationTest {

    private static final String KEYCLOAK_URL = "http://localhost:8100";
    private static final String ACCESS_TOKEN = "test-access-token";
    private static final String FIRST_NAME = "Chuck";
    private static final String LAST_NAME = "Norris";
    private static final String EMAIL = "chuck.norris@example.com";

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Autowired
    private RoleJpaRepository roleJpaRepository;

    @Autowired
    private PermissionJpaRepository permissionJpaRepository;

    @Autowired
    private MockServerRestClientCustomizer mockServerRestClientCustomizer;

    private MockRestServiceServer keycloakMockRestServiceServer;

    @BeforeEach
    void beforeEach() {
        keycloakMockRestServiceServer = mockServerRestClientCustomizer.getServer();
        keycloakMockRestServiceServer.reset();
    }

    @Test
    void testThatGetUserV1IsSuccessful() {
        // given
        UUID id = UUID.randomUUID();
        String suffix = suffix();
        RoleEntity role = createRole(suffix);
        UserEntity user = UserEntity.builder().id(id).build();
        user.addRole(role);
        UserEntity savedUser = userJpaRepository.save(user);

        // when
        ResponseEntity<UserV1Response> responseEntity = testRestTemplate
                .exchange("/internal/api/v1/users/" + id, HttpMethod.GET, TestUtil.jsonHttpEntity(), UserV1Response.class);

        // then
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);

        UserV1Response userV1Response = responseEntity.getBody();
        assertThat(userV1Response).isNotNull();
        assertThat(userV1Response.getId()).isEqualTo(savedUser.getId());
        assertThat(userV1Response.getAuthorities()).containsExactlyInAnyOrder("ROLE_R_" + suffix, "PERMISSION_P_" + suffix);
    }

    @Test
    void testThatGetUserV1ReturnsNotFoundForUnknownUser() {
        // given
        UUID id = UUID.randomUUID();

        // when
        ResponseEntity<ErrorResponseV2> responseEntity = testRestTemplate
                .exchange("/internal/api/v1/users/" + id, HttpMethod.GET, TestUtil.jsonHttpEntity(), ErrorResponseV2.class);

        // then
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(responseEntity.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);

        ErrorResponseV2 errorResponseV2 = responseEntity.getBody();
        assertThat(errorResponseV2).isNotNull();
        assertThat(errorResponseV2.id()).isNotNull();
        assertThat(errorResponseV2.errorCode()).isEqualTo("user.technical.entity-not-found");
        assertThat(errorResponseV2.message()).isEqualTo("User not found by id: " + id);
    }

    @Test
    void testThatCreateUserV1IsSuccessful() {
        // given
        String suffix = suffix();
        String username = "chuck.norris." + suffix;
        RoleEntity role = createRole(suffix);
        UUID id = UUID.randomUUID();
        NewUserV1Request newUserV1Request = NewUserV1Request.builder()
                .username(username)
                .firstName(FIRST_NAME)
                .lastName(LAST_NAME)
                .email(EMAIL)
                .roles(Set.of(role.getName()))
                .build();

        expectAccessToken();
        expectCreateIdentity(username).andRespond(
                withStatus(HttpStatus.CREATED).location(URI.create(KEYCLOAK_URL + "/admin/realms/mra/users/" + id)));

        // when
        ResponseEntity<UserV1Response> responseEntity = testRestTemplate.exchange(
                "/internal/api/v1/users",
                HttpMethod.POST,
                TestUtil.jsonHttpEntity(newUserV1Request),
                UserV1Response.class);

        // then
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(responseEntity.getHeaders().getLocation()).hasPath("/internal/api/v1/users/" + id);

        UserV1Response response = responseEntity.getBody();
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(id);
        assertThat(response.getAuthorities()).containsExactlyInAnyOrder("ROLE_" + role.getName(), "PERMISSION_P_" + suffix);

        UserEntity user = userJpaRepository.findById(id).orElseThrow();
        assertThat(user.isEnabled()).isTrue();

        keycloakMockRestServiceServer.verify();
    }

    @Test
    void testThatCreateUserV1ReturnsConflictAndStoresNothingWhenIdentityExists() {
        // given
        String suffix = suffix();
        String username = "chuck.norris." + suffix;
        RoleEntity role = createRole(suffix);
        NewUserV1Request newUserV1Request = NewUserV1Request.builder()
                .username(username)
                .firstName(FIRST_NAME)
                .lastName(LAST_NAME)
                .email(EMAIL)
                .roles(Set.of(role.getName()))
                .build();
        long userCount = userJpaRepository.count();

        expectAccessToken();
        expectCreateIdentity(username).andRespond(
                withStatus(HttpStatus.CONFLICT).contentType(MediaType.APPLICATION_JSON)
                        .body("{\"errorMessage\":\"User exists with same username\"}"));

        // when
        ResponseEntity<ErrorResponseV2> responseEntity = testRestTemplate.exchange(
                "/internal/api/v1/users",
                HttpMethod.POST,
                TestUtil.jsonHttpEntity(newUserV1Request),
                ErrorResponseV2.class);

        // then
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().errorCode()).isEqualTo("user.business.duplicate-entity");
        assertThat(responseEntity.getBody().message()).isEqualTo("Identity already exists by username or e-mail");
        assertThat(userJpaRepository.count()).isEqualTo(userCount);

        keycloakMockRestServiceServer.verify();
    }

    @Test
    void testThatCreateUserV1ReturnsBadGatewayAndStoresNothingWhenAccessTokenRequestFails() {
        // given
        String suffix = suffix();
        RoleEntity role = createRole(suffix);
        NewUserV1Request newUserV1Request = NewUserV1Request.builder()
                .username("chuck.norris." + suffix)
                .firstName(FIRST_NAME)
                .lastName(LAST_NAME)
                .email(EMAIL)
                .roles(Set.of(role.getName()))
                .build();
        long userCount = userJpaRepository.count();

        keycloakMockRestServiceServer.expect(requestTo(KEYCLOAK_URL + "/realms/mra/protocol/openid-connect/token"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(
                        withStatus(HttpStatus.UNAUTHORIZED).contentType(MediaType.APPLICATION_JSON)
                                .body(
                                        "{\"error\":\"invalid_client\",\"error_description\":\"Invalid client or Invalid client credentials\"}"));

        // when
        ResponseEntity<ErrorResponseV2> responseEntity = testRestTemplate.exchange(
                "/internal/api/v1/users",
                HttpMethod.POST,
                TestUtil.jsonHttpEntity(newUserV1Request),
                ErrorResponseV2.class);

        // then
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_GATEWAY);
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().errorCode()).isEqualTo("user.technical.identity-provider-failure");
        assertThat(responseEntity.getBody().message()).isEqualTo("Identity provider error");
        assertThat(userJpaRepository.count()).isEqualTo(userCount);

        keycloakMockRestServiceServer.verify();
    }

    @Test
    void testThatCreateUserV1DeletesIdentityWhenPersistingFails() {
        // given
        String suffix = suffix();
        String username = "chuck.norris." + suffix;
        RoleEntity role = createRole(suffix);
        UUID id = UUID.randomUUID();
        NewUserV1Request newUserV1Request = NewUserV1Request.builder()
                .username(username)
                .firstName(FIRST_NAME)
                .lastName(LAST_NAME)
                .email(EMAIL)
                .roles(Set.of(role.getName()))
                .build();
        userJpaRepository.save(UserEntity.builder().id(id).build());

        expectAccessToken();
        expectCreateIdentity(username).andRespond(
                withStatus(HttpStatus.CREATED).location(URI.create(KEYCLOAK_URL + "/admin/realms/mra/users/" + id)));
        expectAccessToken();
        keycloakMockRestServiceServer.expect(requestTo(KEYCLOAK_URL + "/admin/realms/mra/users/" + id))
                .andExpect(method(HttpMethod.DELETE))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer " + ACCESS_TOKEN))
                .andRespond(withStatus(HttpStatus.NO_CONTENT));

        // when
        ResponseEntity<ErrorResponseV2> responseEntity = testRestTemplate.exchange(
                "/internal/api/v1/users",
                HttpMethod.POST,
                TestUtil.jsonHttpEntity(newUserV1Request),
                ErrorResponseV2.class);

        // then
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);

        keycloakMockRestServiceServer.verify();
    }

    private void expectAccessToken() {
        keycloakMockRestServiceServer.expect(requestTo(KEYCLOAK_URL + "/realms/mra/protocol/openid-connect/token"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().formDataContains(Map.of("grant_type", "client_credentials")))
                .andRespond(
                        withSuccess(
                                "{\"access_token\":\"" + ACCESS_TOKEN + "\",\"expires_in\":300,\"token_type\":\"Bearer\"}",
                                MediaType.APPLICATION_JSON));
    }

    private ResponseActions expectCreateIdentity(String username) {
        return keycloakMockRestServiceServer.expect(requestTo(KEYCLOAK_URL + "/admin/realms/mra/users"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer " + ACCESS_TOKEN))
                .andExpect(jsonPath("$.username").value(username))
                .andExpect(jsonPath("$.firstName").value(FIRST_NAME))
                .andExpect(jsonPath("$.lastName").value(LAST_NAME))
                .andExpect(jsonPath("$.email").value(EMAIL))
                .andExpect(jsonPath("$.enabled").value(true))
                .andExpect(jsonPath("$.requiredActions[0]").value("UPDATE_PASSWORD"));
    }

    private RoleEntity createRole(String suffix) {
        PermissionEntity permission = permissionJpaRepository.save(PermissionEntity.builder().name("P_" + suffix).build());
        RoleEntity role = RoleEntity.builder().name("R_" + suffix).build();
        role.addPermission(permission);
        return roleJpaRepository.save(role);
    }

    private static String suffix() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

}
