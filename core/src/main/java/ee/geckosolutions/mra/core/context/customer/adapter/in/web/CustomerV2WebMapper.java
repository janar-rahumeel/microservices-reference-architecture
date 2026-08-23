/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright (C) 2026-present Gecko Solutions OÃœ
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

import ee.geckosolutions.mra.common.contract.customer.web.dto.AbstractCustomerV2;
import ee.geckosolutions.mra.common.contract.customer.web.dto.CustomerSearchElementV2Response;
import ee.geckosolutions.mra.common.contract.customer.web.dto.CustomerSearchPageV2Response;
import ee.geckosolutions.mra.common.contract.customer.web.dto.LegalEntityCustomerV2;
import ee.geckosolutions.mra.common.contract.customer.web.dto.PersonCustomerV2;
import ee.geckosolutions.mra.core.context.customer.domain.model.Customer;
import ee.geckosolutions.mra.core.context.customer.domain.model.LegalEntityCustomer;
import ee.geckosolutions.mra.core.context.customer.domain.model.PersonCustomer;

import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.springframework.data.domain.Page;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, builder = @Builder)
public abstract class CustomerV2WebMapper {

    @Mapping(target = "elements", source = "content")
    public abstract CustomerSearchPageV2Response toCustomerSearchPageV2(Page<Customer> page);

    protected CustomerSearchElementV2Response toCustomerSearchElementV2(Customer customer) {
        if (customer instanceof PersonCustomer personCustomer) {
            return toCustomerSearchElementV2(personCustomer);
        }

        if (customer instanceof LegalEntityCustomer legalEntityCustomer) {
            return toCustomerSearchElementV2(legalEntityCustomer);
        }

        throw new IllegalArgumentException("Unsupported customer type: " + customer.getClass().getName());
    }

    @Mapping(target = "type", constant = "PERSON")
    @Mapping(target = "name", expression = "java(customer.getFirstName() + \" \" + customer.getLastName())")
    @Mapping(target = "code", source = "personalIdentificationCode")
    protected abstract CustomerSearchElementV2Response toCustomerSearchElementV2(PersonCustomer customer);

    @Mapping(target = "type", constant = "LEGAL_ENTITY")
    @Mapping(target = "code", source = "registrationCode")
    protected abstract CustomerSearchElementV2Response toCustomerSearchElementV2(LegalEntityCustomer customer);

    public AbstractCustomerV2 toCustomerV2(Customer customer) {
        if (customer instanceof PersonCustomer personCustomer) {
            return toCustomerV2(personCustomer);
        }

        if (customer instanceof LegalEntityCustomer legalEntityCustomer) {
            return toCustomerV2(legalEntityCustomer);
        }

        throw new IllegalArgumentException("Unsupported customer type: " + customer.getClass().getName());
    }

    protected abstract PersonCustomerV2 toCustomerV2(PersonCustomer customer);

    protected abstract LegalEntityCustomerV2 toCustomerV2(LegalEntityCustomer customer);

}
