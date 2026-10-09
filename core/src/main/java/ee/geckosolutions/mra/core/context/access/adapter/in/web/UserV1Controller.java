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

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
import org.springframework.web.bind.annotation.RequestMapping;

@Adapter(direction = AdapterDirection.IN, type = AdapterType.REST, boundedContext = BoundedContext.ACCESS)
@RequestMapping(value = "/internal/api/v1/users")
@ErrorResponseV2Api
@RequiredArgsConstructor
public class UserV1Controller {

    private final UserService userService;

    @GetMapping(value = "/{username}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserV1Response> getByUsername(@PathVariable String username) {
        User user = userService.getByUsername(username);
        Set<String> authorities = Stream
                .concat(
                        user.getRoles().stream().map(role -> "ROLE_" + role.getName()),
                        user.getRoles()
                                .stream()
                                .flatMap(role -> role.getPermissions().stream())
                                .map(permission -> "PERMISSION_" + permission.getName()))
                .collect(Collectors.toSet());
        return ResponseEntity
                .ok(UserV1Response.builder().id(user.getId()).username(user.getUsername()).authorities(authorities).build());
    }

}
