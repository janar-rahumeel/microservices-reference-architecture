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
package ee.geckosolutions.mra.core.context.access.adapter.out.persistence;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import ee.geckosolutions.mra.common.platform.observation.Adapter;
import ee.geckosolutions.mra.common.platform.observation.AdapterDirection;
import ee.geckosolutions.mra.common.platform.observation.AdapterType;
import ee.geckosolutions.mra.common.platform.observation.BoundedContext;
import ee.geckosolutions.mra.core.context.access.adapter.out.persistence.entity.UserEntity;
import ee.geckosolutions.mra.core.context.access.adapter.out.persistence.repository.RoleJpaRepository;
import ee.geckosolutions.mra.core.context.access.adapter.out.persistence.repository.UserJpaRepository;
import ee.geckosolutions.mra.core.context.access.application.port.UserRepository;
import ee.geckosolutions.mra.core.context.access.domain.model.Role;
import ee.geckosolutions.mra.core.context.access.domain.model.User;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Adapter(direction = AdapterDirection.OUT, type = AdapterType.JPA, boundedContext = BoundedContext.ACCESS)
@Repository
@RequiredArgsConstructor
public class PostgresUserRepository implements UserRepository {

    private final UserJpaRepository userJpaRepository;
    private final RoleJpaRepository roleJpaRepository;

    @Override
    public Optional<User> findById(UUID id) {
        return userJpaRepository.findById(id).map(PostgresUserRepository::toDomain);
    }

    @Override
    @Transactional
    public User create(User user) {
        UserEntity userEntity = UserEntity.builder().id(user.getId()).enabled(user.isEnabled()).build();
        Set<String> roleNames = user.getRoles().stream().map(Role::getName).collect(Collectors.toSet());
        roleJpaRepository.findAllByNameIn(roleNames).forEach(userEntity::addRole);
        return toDomain(userJpaRepository.saveAndFlush(userEntity));
    }

    private static User toDomain(UserEntity user) {
        Set<Role> roles = user.getRoles().stream().map(PostgresRoleRepository::toDomain).collect(Collectors.toSet());
        return new User(user.getId(), user.isEnabled(), roles);
    }

}
