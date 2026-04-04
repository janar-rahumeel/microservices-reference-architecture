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
package ee.geckosolutions.mra.core.context.customer.domain.model;

import java.util.UUID;

import lombok.Getter;

@Getter
public class PersonCustomer extends Customer {

    private final String firstName;
    private final String lastName;
    private final String personalIdentificationCode;

    public PersonCustomer(UUID id, String firstName, String lastName, String personalIdentificationCode) {
        super(id, CustomerType.PERSON);
        this.firstName = requireNonBlank(firstName, "firstName");
        this.lastName = requireNonBlank(lastName, "lastName");
        this.personalIdentificationCode = requireNonBlank(personalIdentificationCode, "personalIdentificationCode");
    }

    public static PersonCustomer createNew(String firstName, String lastName, String personalIdentificationCode) {
        return new PersonCustomer(UUID.randomUUID(), firstName, lastName, personalIdentificationCode);
    }

}
