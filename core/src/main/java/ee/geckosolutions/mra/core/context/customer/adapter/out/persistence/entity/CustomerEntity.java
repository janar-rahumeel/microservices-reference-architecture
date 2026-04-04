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

import ee.geckosolutions.mra.core.context.customer.domain.model.CustomerType;

import lombok.Getter;
import lombok.Setter;

@Table(name = "CUSTOMER")
@Getter
@Setter
@Entity
public class CustomerEntity {

    @Id
    @Column(name = "ID")
    private UUID id;

    @Column(name = "TYPE")
    @Enumerated(EnumType.STRING)
    private CustomerType type;

    @Column(name = "FIRST_NAME")
    private String firstName;

    @Column(name = "LAST_NAME")
    private String lastName;

    @Column(name = "PERSONAL_IDENTIFICATION_CODE")
    private String personalIdentificationCode;

    @Column(name = "NAME")
    private String name;

    @Column(name = "REGISTRATION_CODE")
    private String registrationCode;

}
