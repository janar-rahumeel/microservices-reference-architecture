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

import java.net.URI;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jakarta.validation.Valid;

import ee.geckosolutions.mra.common.contract.access.web.dto.NewUserV1Request;
import ee.geckosolutions.mra.common.contract.access.web.dto.UserV1Response;
import ee.geckosolutions.mra.common.platform.observation.Adapter;
import ee.geckosolutions.mra.common.platform.observation.AdapterDirection;
import ee.geckosolutions.mra.common.platform.observation.AdapterType;
import ee.geckosolutions.mra.common.platform.observation.BoundedContext;
import ee.geckosolutions.mra.common.platform.web.ErrorResponseV2Api;
import ee.geckosolutions.mra.core.context.access.application.UserService;
import ee.geckosolutions.mra.core.context.access.domain.model.User;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.util.UriComponentsBuilder;

@Adapter(direction = AdapterDirection.IN, type = AdapterType.REST, boundedContext = BoundedContext.ACCESS)
@RequestMapping(value = "/internal/api/v1/users")
@ErrorResponseV2Api
@RequiredArgsConstructor
public class UserV1Controller {

    private final UserService userService;

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserV1Response> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(toResponse(userService.getById(id)));
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserV1Response> create(@Valid @RequestBody NewUserV1Request request) {
        User user = userService.createUser(
                request.getUsername(),
                request.getFirstName(),
                request.getLastName(),
                request.getEmail(),
                request.getRoles());
        URI location = UriComponentsBuilder.fromPath("/internal/api/v1/users/{id}").build(user.getId());
        return ResponseEntity.created(location).body(toResponse(user));
    }

    private static UserV1Response toResponse(User user) {
        Set<String> authorities = Stream
                .concat(
                        user.getRoles().stream().map(role -> "ROLE_" + role.getName()),
                        user.getRoles()
                                .stream()
                                .flatMap(role -> role.getPermissions().stream())
                                .map(permission -> "PERMISSION_" + permission.getName()))
                .collect(Collectors.toSet());
        return UserV1Response.builder().id(user.getId()).authorities(authorities).build();
    }

}
