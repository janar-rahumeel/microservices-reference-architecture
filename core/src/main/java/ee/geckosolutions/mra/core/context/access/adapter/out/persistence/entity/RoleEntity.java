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
package ee.geckosolutions.mra.core.context.access.adapter.out.persistence.entity;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;

import ee.geckosolutions.mra.common.domain.identifier.UuidV7;
import ee.geckosolutions.mra.common.platform.persistence.AbstractEntity;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Table(name = "role")
@Getter
@Setter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RoleEntity extends AbstractEntity<UUID> {

    @Id
    @Column(name = "id")
    @Setter(AccessLevel.NONE)
    private UUID id;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "enabled")
    private boolean enabled = true;

    @Setter(AccessLevel.NONE)
    @ManyToMany(mappedBy = "roles", fetch = FetchType.LAZY)
    private Set<UserEntity> users = new HashSet<>();

    @Setter(AccessLevel.NONE)
    @JoinTable(
            name = "role_permission",
            joinColumns = @JoinColumn(name = "role_id"),
            inverseJoinColumns = @JoinColumn(name = "permission_id"))
    @ManyToMany(fetch = FetchType.LAZY)
    private Set<PermissionEntity> permissions = new HashSet<>();

    @Builder(access = AccessLevel.PUBLIC)
    private RoleEntity(String name, String description, Boolean enabled) {
        this.id = UuidV7.generate();
        this.name = name;
        this.description = description;
        this.enabled = enabled == null || enabled;
    }

    public void addPermission(PermissionEntity permission) {
        if (permissions.add(permission)) {
            permission.getRoles().add(this);
        }
    }

    public void removePermission(PermissionEntity permission) {
        if (permissions.remove(permission)) {
            permission.getRoles().remove(this);
        }
    }

}
