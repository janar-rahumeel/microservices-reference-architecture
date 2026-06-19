/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright (C) 2026-present Gecko Solutions OU
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

import static org.assertj.core.api.Assertions.assertThat;

import ee.geckosolutions.mra.common.contract.customer.web.dto.LegalEntityCustomerV2;
import ee.geckosolutions.mra.common.contract.customer.web.dto.NewLegalEntityCustomerV2;
import ee.geckosolutions.mra.common.contract.customer.web.dto.PersonCustomerV2;
import ee.geckosolutions.mra.common.platform.web.dto.ErrorResponseV2;
import ee.geckosolutions.mra.core.context.customer.application.CustomerApplicationService;
import ee.geckosolutions.mra.core.context.customer.domain.model.Customer;
import ee.geckosolutions.mra.core.context.customer.domain.model.LegalEntityCustomer;
import ee.geckosolutions.mra.core.context.customer.domain.model.PersonCustomer;
import ee.geckosolutions.mra.core.test.AbstractWebIntegrationTest;
import ee.geckosolutions.mra.core.test.TestUtil;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

class CustomerV2ControllerIntegrationTest extends AbstractWebIntegrationTest {

    @Autowired
    private CustomerApplicationService customerApplicationService;

    @Test
    void testThatGetCustomerV2IsSuccessful() {
        // given
        PersonCustomer personCustomer = (PersonCustomer) customerApplicationService
                .createPersonCustomer("Chuck", "Norris", "38109239860");

        // when
        ResponseEntity<PersonCustomerV2> responseEntity = testRestTemplate.exchange(
                "/internal/api/v2/customers/" + personCustomer.getId(),
                HttpMethod.GET,
                TestUtil.jsonHttpEntity(),
                PersonCustomerV2.class);

        // then
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);

        PersonCustomerV2 personCustomerV2 = responseEntity.getBody();
        assertThat(personCustomerV2).isNotNull();
        assertThat(personCustomerV2.getId()).isEqualTo(personCustomer.getId());
        assertThat(personCustomerV2.getFirstName()).isEqualTo(personCustomer.getFirstName());
        assertThat(personCustomerV2.getLastName()).isEqualTo(personCustomer.getLastName());
        assertThat(personCustomerV2.getPersonalIdentificationCode()).isEqualTo(personCustomer.getPersonalIdentificationCode());
    }

    @Test
    void testThatInsertCustomerV2IsSuccessful() {
        // given
        NewLegalEntityCustomerV2 newLegalEntityCustomerV2 = NewLegalEntityCustomerV2.builder()
                .name("Chuck Norris Ltd")
                .registrationCode("14447333")
                .build();

        // when
        ResponseEntity<LegalEntityCustomerV2> responseEntity = testRestTemplate.exchange(
                "/internal/api/v2/customers",
                HttpMethod.POST,
                TestUtil.jsonHttpEntity(newLegalEntityCustomerV2),
                LegalEntityCustomerV2.class);

        // then
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);

        LegalEntityCustomerV2 legalEntityCustomerV2 = responseEntity.getBody();
        assertThat(legalEntityCustomerV2).isNotNull();
        assertThat(legalEntityCustomerV2.getId()).isNotNull();
        assertThat(legalEntityCustomerV2.getName()).isEqualTo(newLegalEntityCustomerV2.getName());
        assertThat(legalEntityCustomerV2.getRegistrationCode()).isEqualTo(newLegalEntityCustomerV2.getRegistrationCode());

        Customer customer = customerApplicationService.getById(legalEntityCustomerV2.getId());
        assertThat(customer).isInstanceOf(LegalEntityCustomer.class);
    }

    @Test
    void testThatInsertCustomerV2IsNotSuccessfulWhenDuplicateCustomer() {
        // given
        NewLegalEntityCustomerV2 newLegalEntityCustomerV2 = NewLegalEntityCustomerV2.builder()
                .name("Duplicate Ltd")
                .registrationCode("14447334")
                .build();
        customerApplicationService
                .createLegalEntityCustomer(newLegalEntityCustomerV2.getName(), newLegalEntityCustomerV2.getRegistrationCode());

        // when
        ResponseEntity<ErrorResponseV2> responseEntity = testRestTemplate.exchange(
                "/internal/api/v2/customers",
                HttpMethod.POST,
                TestUtil.jsonHttpEntity(newLegalEntityCustomerV2),
                ErrorResponseV2.class);

        // then
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(responseEntity.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);

        ErrorResponseV2 errorResponseV2 = responseEntity.getBody();
        assertThat(errorResponseV2).isNotNull();
        assertThat(errorResponseV2.id()).isNotNull();
        assertThat(errorResponseV2.errorCode().getValue()).isEqualTo("customer.business.duplicate-entity");
        assertThat(errorResponseV2.message()).isEqualTo(
                "Legal entity with registration code already exists: " + newLegalEntityCustomerV2.getRegistrationCode());
    }

}
