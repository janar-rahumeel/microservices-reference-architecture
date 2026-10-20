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

import java.util.Set;
import java.util.stream.Collectors;

import ee.geckosolutions.mra.common.platform.observation.Adapter;
import ee.geckosolutions.mra.common.platform.observation.AdapterDirection;
import ee.geckosolutions.mra.common.platform.observation.AdapterType;
import ee.geckosolutions.mra.common.platform.observation.BoundedContext;
import ee.geckosolutions.mra.core.context.access.adapter.out.persistence.entity.RoleEntity;
import ee.geckosolutions.mra.core.context.access.adapter.out.persistence.repository.RoleJpaRepository;
import ee.geckosolutions.mra.core.context.access.application.port.RoleRepository;
import ee.geckosolutions.mra.core.context.access.domain.model.Permission;
import ee.geckosolutions.mra.core.context.access.domain.model.Role;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Adapter(direction = AdapterDirection.OUT, type = AdapterType.JPA, boundedContext = BoundedContext.ACCESS)
@Repository
@RequiredArgsConstructor
public class PostgresRoleRepository implements RoleRepository {

    private final RoleJpaRepository roleJpaRepository;

    @Override
    @Transactional(readOnly = true)
    public Set<Role> findByNames(Set<String> names) {
        return roleJpaRepository.findAllByNameIn(names)
                .stream()
                .map(PostgresRoleRepository::toDomain)
                .collect(Collectors.toSet());
    }

    static Role toDomain(RoleEntity role) {
        Set<Permission> permissions = role.getPermissions()
                .stream()
                .map(permission -> new Permission(permission.getName()))
                .collect(Collectors.toSet());
        return new Role(role.getName(), role.isEnabled(), permissions);
    }

}
