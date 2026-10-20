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
package ee.geckosolutions.mra.gateway.context.access.adapter.out.api;

import java.util.Optional;
import java.util.UUID;

import ee.geckosolutions.mra.common.contract.access.web.dto.UserV1Response;
import ee.geckosolutions.mra.gateway.context.access.application.port.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthenticatedUserRepository implements UserRepository {

    private final InternalUserV1Client internalUserV1Client;

    @Override
    public Optional<User> findById(UUID id) {
        ResponseEntity<UserV1Response> responseEntity = internalUserV1Client.get(id);
        HttpStatusCode httpStatusCode = responseEntity.getStatusCode();

        if (httpStatusCode.isSameCodeAs(HttpStatus.OK)) {
            return Optional.ofNullable(responseEntity.getBody()).map(this::toUser).or(() -> {
                throw new IllegalStateException("Internal user service returned an empty body for id '%s'".formatted(id));
            });
        }

        if (httpStatusCode.isSameCodeAs(HttpStatus.NOT_FOUND)) {
            return Optional.empty();
        }

        throw new IllegalStateException(
                "Internal user service returned unexpected HTTP status '%s' for id '%s'".formatted(httpStatusCode, id));
    }

    private User toUser(UserV1Response response) {
        return new User("masked", null, AuthorityUtils.createAuthorityList(response.getAuthorities()));
    }

}
