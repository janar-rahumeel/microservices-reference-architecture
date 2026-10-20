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
package ee.geckosolutions.mra.core.context.access.domain.model;

import java.util.Set;
import java.util.UUID;

import ee.geckosolutions.mra.common.domain.AggregateRoot;
import ee.geckosolutions.mra.common.domain.validation.Invariants;

import lombok.Getter;

@Getter
public final class User extends AggregateRoot {

    private final UUID id;
    private final boolean enabled;
    private final Set<Role> roles;

    public User(UUID id, boolean enabled, Set<Role> roles) {
        Invariants.check().notNull("id", id).notEmpty("roles", roles).throwIfViolated();
        this.id = id;
        this.enabled = enabled;
        this.roles = roles;
    }

    public static User create(UUID id, Set<Role> roles) {
        return new User(id, true, roles);
    }

}
