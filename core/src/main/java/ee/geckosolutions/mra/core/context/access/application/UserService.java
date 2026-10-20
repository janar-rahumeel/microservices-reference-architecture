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
package ee.geckosolutions.mra.core.context.access.application;

import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.stream.Collectors;

import ee.geckosolutions.mra.common.domain.validation.Invariants;
import ee.geckosolutions.mra.common.platform.observation.ApplicationService;
import ee.geckosolutions.mra.common.platform.observation.BoundedContext;
import ee.geckosolutions.mra.core.context.access.application.exception.UserNotFoundException;
import ee.geckosolutions.mra.core.context.access.application.port.IdentityProvider;
import ee.geckosolutions.mra.core.context.access.application.port.IdentityProvider.NewIdentity;
import ee.geckosolutions.mra.core.context.access.application.port.RoleRepository;
import ee.geckosolutions.mra.core.context.access.application.port.UserRepository;
import ee.geckosolutions.mra.core.context.access.domain.model.Role;
import ee.geckosolutions.mra.core.context.access.domain.model.User;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@ApplicationService(boundedContext = BoundedContext.ACCESS)
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final IdentityProvider identityProvider;

    @Transactional(readOnly = true)
    public User getById(UUID id) {
        return userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
    }

    public User createUser(String username, String firstName, String lastName, String email, Set<String> roleNames) {
        Set<Role> roles = resolveRoles(roleNames);
        UUID identityUserId = identityProvider.createUser(new NewIdentity(username, firstName, lastName, email));
        try {
            return userRepository.create(User.create(identityUserId, roles));
        } catch (Exception e) {
            revert(identityUserId, e);
            throw e;
        }
    }

    private Set<Role> resolveRoles(Set<String> roleNames) {
        Set<Role> roles = roleRepository.findByNames(roleNames);
        Set<String> unknownRoleNames = new TreeSet<>(roleNames);
        unknownRoleNames.removeAll(roles.stream().map(Role::getName).collect(Collectors.toSet()));
        Invariants.check()
                .that(unknownRoleNames.isEmpty(), "roles", "validation.unknown-role", Map.of("names", unknownRoleNames))
                .throwIfViolated();
        return roles;
    }

    private void revert(UUID identityUserId, Exception cause) {
        try {
            identityProvider.deleteUser(identityUserId);
        } catch (Exception e) {
            log.error("Failed to delete orphaned identity {}", identityUserId, e);
            cause.addSuppressed(e);
        }
    }

}
