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
package ee.geckosolutions.mra.core.context.customer.adapter.out.persistence;

import java.util.Optional;
import java.util.UUID;

import ee.geckosolutions.mra.core.context.customer.adapter.out.persistence.entity.CustomerEntity;
import ee.geckosolutions.mra.core.context.customer.adapter.out.persistence.repository.JpaCustomerRepository;
import ee.geckosolutions.mra.core.context.customer.application.port.CustomerRepository;
import ee.geckosolutions.mra.core.context.customer.domain.model.Customer;
import ee.geckosolutions.mra.core.context.customer.domain.model.CustomerType;
import ee.geckosolutions.mra.core.context.customer.domain.model.LegalEntityCustomer;
import ee.geckosolutions.mra.core.context.customer.domain.model.PersonCustomer;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class CustomerRepositoryImpl implements CustomerRepository {

    private final JpaCustomerRepository jpaCustomerRepository;

    @Override
    public Optional<Customer> findById(UUID id) {
        return jpaCustomerRepository.findById(id).map(CustomerRepositoryImpl::toDomain);
    }

    @Override
    public boolean personExistsByPersonalIdentificationCode(String personalIdentificationCode) {
        return jpaCustomerRepository.existsByPersonalIdentificationCode(personalIdentificationCode);
    }

    @Override
    public boolean legalEntityExistsByRegistrationCode(String registrationCode) {
        return jpaCustomerRepository.existsByRegistrationCode(registrationCode);
    }

    @Override
    public Customer create(Customer customer) {
        CustomerEntity customerEntity = toEntity(customer);
        return toDomain(jpaCustomerRepository.save(customerEntity));
    }

    private static CustomerEntity toEntity(Customer customer) {
        CustomerEntity customerEntity = new CustomerEntity();
        customerEntity.setId(customer.getId());
        customerEntity.setType(customer.getType());

        if (customer instanceof PersonCustomer personCustomer) {
            customerEntity.setFirstName(personCustomer.getFirstName());
            customerEntity.setLastName(personCustomer.getLastName());
            customerEntity.setPersonalIdentificationCode(personCustomer.getPersonalIdentificationCode());
            return customerEntity;
        }

        if (customer instanceof LegalEntityCustomer legalEntityCustomer) {
            customerEntity.setName(legalEntityCustomer.getName());
            customerEntity.setRegistrationCode(legalEntityCustomer.getRegistrationCode());
            return customerEntity;
        }

        throw new IllegalArgumentException("Unsupported customer type: " + customer.getClass().getName());
    }

    private static Customer toDomain(CustomerEntity entity) {
        if (entity.getType() == CustomerType.PERSON) {
            return new PersonCustomer(
                    entity.getId(),
                    entity.getFirstName(),
                    entity.getLastName(),
                    entity.getPersonalIdentificationCode());
        }

        if (entity.getType() == CustomerType.LEGAL_ENTITY) {
            return new LegalEntityCustomer(entity.getId(), entity.getName(), entity.getRegistrationCode());
        }

        throw new IllegalArgumentException("Unsupported customer type in persistence: " + entity.getType());
    }

}
