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
package ee.geckosolutions.mra.core.context.customer.application;

import java.util.UUID;

import ee.geckosolutions.mra.core.context.customer.application.exception.CustomerNotFoundException;
import ee.geckosolutions.mra.core.context.customer.application.port.CustomerRepository;
import ee.geckosolutions.mra.core.context.customer.domain.exception.DuplicateCustomerException;
import ee.geckosolutions.mra.core.context.customer.domain.model.Customer;
import ee.geckosolutions.mra.core.context.customer.domain.model.LegalEntityCustomer;
import ee.geckosolutions.mra.core.context.customer.domain.model.PersonCustomer;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomerApplicationService {

    private final CustomerRepository customerRepository;

    @Transactional(readOnly = true)
    public Customer getById(UUID id) {
        return customerRepository.findById(id).orElseThrow(() -> new CustomerNotFoundException(id));
    }

    @Transactional
    public Customer createPersonCustomer(String firstName, String lastName, String personalIdentificationCode) {
        PersonCustomer customer = PersonCustomer.createNew(firstName, lastName, personalIdentificationCode);

        if (customerRepository.personExistsByPersonalIdentificationCode(customer.getPersonalIdentificationCode())) {
            throw new DuplicateCustomerException(
                    "Person with personal identification code already exists: " + customer.getPersonalIdentificationCode());
        }

        return customerRepository.create(customer);
    }

    @Transactional
    public Customer createLegalEntityCustomer(String name, String registrationCode) {
        LegalEntityCustomer customer = LegalEntityCustomer.createNew(name, registrationCode);

        if (customerRepository.legalEntityExistsByRegistrationCode(customer.getRegistrationCode())) {
            throw new DuplicateCustomerException(
                    "Legal entity with registration code already exists: " + customer.getRegistrationCode());
        }

        return customerRepository.create(customer);
    }

}
