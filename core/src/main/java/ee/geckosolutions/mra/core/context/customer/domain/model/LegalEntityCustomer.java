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

import ee.geckosolutions.mra.common.domain.identifier.UuidV7;
import ee.geckosolutions.mra.common.domain.validation.Invariants;
import ee.geckosolutions.mra.core.context.customer.domain.event.CustomerCreatedEvent;

import lombok.Getter;

@Getter
public class LegalEntityCustomer extends Customer {

    private final String name;
    private final String registrationCode;

    public LegalEntityCustomer(UUID id, String name, String registrationCode) {
        super(id, CustomerType.LEGAL_ENTITY);
        Invariants.check().notBlank("name", name).notBlank("registrationCode", registrationCode).throwIfViolated();
        this.name = name;
        this.registrationCode = registrationCode;
    }

    public static LegalEntityCustomer createNew(String name, String registrationCode) {
        LegalEntityCustomer legalEntityCustomer = new LegalEntityCustomer(UuidV7.generate(), name, registrationCode);
        legalEntityCustomer.registerEvent(new CustomerCreatedEvent(legalEntityCustomer.getId()));
        return legalEntityCustomer;
    }

}
