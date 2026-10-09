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
package ee.geckosolutions.mra.core.context.customer.adapter.out.persistence.entity;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import ee.geckosolutions.mra.common.platform.persistence.AbstractEntity;
import ee.geckosolutions.mra.core.context.customer.domain.model.CustomerType;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Table(name = "customer")
@Getter
@Setter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CustomerEntity extends AbstractEntity<UUID> {

    @Id
    @Setter(AccessLevel.NONE)
    @Column(name = "id")
    private UUID id;

    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    private CustomerType type;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "personal_identification_code")
    private String personalIdentificationCode;

    @Column(name = "name")
    private String name;

    @Column(name = "registration_code")
    private String registrationCode;

    @Builder(access = AccessLevel.PUBLIC)
    private CustomerEntity(
            UUID id,
            CustomerType type,
            String firstName,
            String lastName,
            String personalIdentificationCode,
            String name,
            String registrationCode) {
        this.id = id;
        this.type = type;
        this.firstName = firstName;
        this.lastName = lastName;
        this.personalIdentificationCode = personalIdentificationCode;
        this.name = name;
        this.registrationCode = registrationCode;
    }

}
