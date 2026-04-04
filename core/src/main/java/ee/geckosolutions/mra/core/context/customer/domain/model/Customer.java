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

import ee.geckosolutions.mra.core.context.customer.domain.exception.InvalidCustomerFieldException;

public abstract class Customer {

    private final UUID id;
    private final CustomerType type;

    protected Customer(UUID id, CustomerType type) {
        this.id = requireNonBlank(id, "id");
        this.type = requireNonBlank(type, "type");
    }

    protected static <T> T requireNonBlank(T value, String fieldName) {
        if (value == null || (value instanceof String stringValue && stringValue.isBlank())) {
            throw new InvalidCustomerFieldException(
                    "customer.technical.field-validation",
                    String.format("'%s' must not be blank", fieldName));
        }
        return value;
    }

    public UUID getId() {
        return this.id;
    }

    public CustomerType getType() {
        return this.type;
    }

}
