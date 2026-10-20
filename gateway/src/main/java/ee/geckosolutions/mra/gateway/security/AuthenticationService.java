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
package ee.geckosolutions.mra.gateway.security;

import java.util.UUID;

import ee.geckosolutions.mra.common.platform.observation.ApplicationService;
import ee.geckosolutions.mra.gateway.context.access.application.port.UserRepository;
import ee.geckosolutions.mra.gateway.security.config.SecurityConfiguration;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;

@ApplicationService
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;

    @Cacheable(cacheNames = SecurityConfiguration.USER_CACHE_NAME, key = "#id")
    public User getUserById(UUID id) throws AuthenticationException {
        return userRepository.findById(id)
                .orElseThrow(() -> new AuthenticatedUserNotFoundException("User not found by id: " + id));
    }

}
