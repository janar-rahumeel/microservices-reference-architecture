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

import ee.geckosolutions.mra.common.contract.customer.web.dto.CustomerV1;
import ee.geckosolutions.mra.common.contract.customer.web.dto.NewCustomerV1;
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
import org.springframework.web.bind.annotation.RestController;

@Deprecated(forRemoval = true)
@RestController
@RequestMapping(value = "/internal/api/v1/customers")
@RequiredArgsConstructor
public class CustomerV1Controller {

    private final CustomerApplicationService customerApplicationService;
    private final CustomerV1WebMapper customerV1WebMapper;

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CustomerV1> get(@PathVariable UUID id) {
        CustomerV1 customerV1 = customerV1WebMapper.toCustomerV1(customerApplicationService.getById(id));
        return ResponseEntity.ok(customerV1);
    }

    @PostMapping(value = "", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CustomerV1> insert(@Valid @RequestBody NewCustomerV1 newCustomerV1) {
        Customer customer = switch (newCustomerV1.getType()) {
        case PRIVATE -> customerApplicationService.createPersonCustomer(
                newCustomerV1.getFirstName(),
                newCustomerV1.getLastName(),
                newCustomerV1.getPersonalIdentificationCode());
        case COMPANY ->
            customerApplicationService.createLegalEntityCustomer(newCustomerV1.getName(), newCustomerV1.getRegistrationCode());
        };
        CustomerV1 customerV1 = customerV1WebMapper.toCustomerV1(customer);
        return ResponseEntity.ok(customerV1);
    }

}
