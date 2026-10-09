/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright (C) 2026-present Gecko Solutions OU
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

import java.util.UUID;

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

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

class UserV1ControllerIntegrationTest extends AbstractWebIntegrationTest {

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Autowired
    private RoleJpaRepository roleJpaRepository;

    @Autowired
    private PermissionJpaRepository permissionJpaRepository;

    @Test
    void testThatGetUserV1IsSuccessful() {
        // given
        String username = "test";
        PermissionEntity permission = permissionJpaRepository.save(PermissionEntity.builder().name("CUSTOMER_READ").build());
        RoleEntity role = RoleEntity.builder().name("USER").build();
        role.addPermission(permission);
        roleJpaRepository.save(role);
        UserEntity user = UserEntity.builder().username(username).build();
        user.addRole(role);
        UserEntity savedUser = userJpaRepository.save(user);

        // when
        ResponseEntity<UserV1Response> responseEntity = testRestTemplate.exchange(
                "/internal/api/v1/users/" + username,
                HttpMethod.GET,
                TestUtil.jsonHttpEntity(),
                UserV1Response.class);

        // then
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);

        UserV1Response userV1Response = responseEntity.getBody();
        assertThat(userV1Response).isNotNull();
        assertThat(userV1Response.getId()).isEqualTo(savedUser.getId());
        assertThat(userV1Response.getUsername()).isEqualTo(username);
        assertThat(userV1Response.getAuthorities()).containsExactly("ROLE_USER", "PERMISSION_CUSTOMER_READ");
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
        assertThat(errorResponseV2.errorCode().getValue()).isEqualTo("user.technical.entity-not-found");
        assertThat(errorResponseV2.message()).isEqualTo("User not found by username: " + id);
    }

}
