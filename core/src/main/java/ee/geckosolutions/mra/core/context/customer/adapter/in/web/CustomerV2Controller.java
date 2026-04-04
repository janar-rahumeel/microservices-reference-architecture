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
package ee.geckosolutions.mra.core.context.customer.adapter.in.web;

import java.util.UUID;

import jakarta.validation.Valid;

import ee.geckosolutions.mra.common.contract.customer.web.dto.AbstractCustomerV2;
import ee.geckosolutions.mra.common.contract.customer.web.dto.AbstractNewCustomerV2;
import ee.geckosolutions.mra.common.contract.customer.web.dto.NewLegalEntityCustomerV2;
import ee.geckosolutions.mra.common.contract.customer.web.dto.NewPersonCustomerV2;
import ee.geckosolutions.mra.common.platform.web.ErrorResponseV2Api;
import ee.geckosolutions.mra.core.context.customer.application.CustomerApplicationService;
import ee.geckosolutions.mra.core.context.customer.domain.model.Customer;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping(value = "/internal/api/v2/customers")
@ErrorResponseV2Api
@RequiredArgsConstructor
public class CustomerV2Controller {

    private final CustomerApplicationService customerApplicationService;
    private final CustomerV2WebMapper customerV2WebMapper;

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AbstractCustomerV2> get(@PathVariable UUID id) {
        AbstractCustomerV2 customerV2 = customerV2WebMapper.toCustomerV2(customerApplicationService.getById(id));
        return ResponseEntity.ok(customerV2);
    }

    @PostMapping(value = "", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AbstractCustomerV2> insert(@Valid @RequestBody AbstractNewCustomerV2 newCustomerV2) {
        Customer customer = insertCustomer(newCustomerV2);
        AbstractCustomerV2 customerV2 = customerV2WebMapper.toCustomerV2(customer);
        return ResponseEntity.ok(customerV2);
    }

    private Customer insertCustomer(AbstractNewCustomerV2 newCustomerV2) {
        if (newCustomerV2 instanceof NewPersonCustomerV2 newPersonCustomerV2) {
            return customerApplicationService.createPersonCustomer(
                    newPersonCustomerV2.getFirstName(),
                    newPersonCustomerV2.getLastName(),
                    newPersonCustomerV2.getPersonalIdentificationCode());
        }

        NewLegalEntityCustomerV2 newLegalEntityCustomerV2 = (NewLegalEntityCustomerV2) newCustomerV2;
        return customerApplicationService
                .createLegalEntityCustomer(newLegalEntityCustomerV2.getName(), newLegalEntityCustomerV2.getRegistrationCode());
    }

}
