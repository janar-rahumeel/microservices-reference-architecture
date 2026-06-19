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

import java.net.URI;

import ee.geckosolutions.mra.common.contract.customer.web.dto.CustomerTypeV1;
import ee.geckosolutions.mra.common.contract.customer.web.dto.CustomerV1;
import ee.geckosolutions.mra.common.contract.customer.web.dto.NewCustomerV1;
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
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

class CustomerV1ControllerIntegrationTest extends AbstractWebIntegrationTest {

    @Autowired
    private CustomerApplicationService customerApplicationService;

    @Test
    void testThatGetCustomerV1IsSuccessful() {
        // given
        PersonCustomer personCustomer = (PersonCustomer) customerApplicationService
                .createPersonCustomer("Chuck", "Norris", "38109239859");

        // when
        ResponseEntity<CustomerV1> responseEntity = testRestTemplate.exchange(
                "/internal/api/v1/customers/" + personCustomer.getId(),
                HttpMethod.GET,
                TestUtil.jsonHttpEntity(),
                CustomerV1.class);

        // then
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);

        CustomerV1 customerV1 = responseEntity.getBody();
        assertThat(customerV1).isNotNull();
        assertThat(customerV1.getId()).isEqualTo(personCustomer.getId());
        assertThat(customerV1.getType()).isEqualTo(CustomerTypeV1.PRIVATE);
        assertThat(customerV1.getFirstName()).isEqualTo(personCustomer.getFirstName());
        assertThat(customerV1.getLastName()).isEqualTo(personCustomer.getLastName());
        assertThat(customerV1.getPersonalIdentificationCode()).isEqualTo(personCustomer.getPersonalIdentificationCode());
        assertThat(customerV1.getName()).isNull();
        assertThat(customerV1.getRegistrationCode()).isNull();
    }

    @Test
    void testThatInsertCustomerV1IsSuccessful() {
        // given
        NewCustomerV1 newCustomerV1 = NewCustomerV1.builder()
                .type(CustomerTypeV1.COMPANY)
                .name("Chuck Norris Ltd")
                .registrationCode("14447331")
                .build();

        // when
        ResponseEntity<CustomerV1> responseEntity = testRestTemplate.exchange(
                "/internal/api/v1/customers",
                HttpMethod.POST,
                TestUtil.jsonHttpEntity(newCustomerV1),
                CustomerV1.class);

        // then
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);

        CustomerV1 customerV1 = responseEntity.getBody();
        assertThat(customerV1).isNotNull();
        assertThat(customerV1.getId()).isNotNull();
        assertThat(customerV1.getType()).isEqualTo(CustomerTypeV1.COMPANY);
        assertThat(customerV1.getName()).isEqualTo(newCustomerV1.getName());
        assertThat(customerV1.getRegistrationCode()).isEqualTo(newCustomerV1.getRegistrationCode());
        assertThat(customerV1.getFirstName()).isNull();
        assertThat(customerV1.getLastName()).isNull();
        assertThat(customerV1.getPersonalIdentificationCode()).isNull();

        Customer customer = customerApplicationService.getById(customerV1.getId());
        assertThat(customer).isInstanceOf(LegalEntityCustomer.class);
    }

    @Test
    void testThatInsertCustomerV1IsNotSuccessfulWhenDuplicateCustomer() {
        // given
        NewCustomerV1 newCustomerV1 = NewCustomerV1.builder()
                .type(CustomerTypeV1.COMPANY)
                .name("Duplicate Ltd")
                .registrationCode("14447332")
                .build();
        customerApplicationService.createLegalEntityCustomer(newCustomerV1.getName(), newCustomerV1.getRegistrationCode());

        // when
        ResponseEntity<ProblemDetail> responseEntity = testRestTemplate.exchange(
                "/internal/api/v1/customers",
                HttpMethod.POST,
                TestUtil.jsonHttpEntity(newCustomerV1),
                ProblemDetail.class);

        // then
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(responseEntity.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_PROBLEM_JSON);

        ProblemDetail problemDetail = responseEntity.getBody();
        assertThat(problemDetail).isNotNull();
        assertThat(problemDetail.getTitle()).isEqualTo("Internal Server Error");
        assertThat(problemDetail.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
        assertThat(problemDetail.getDetail()).isEqualTo("An unexpected error occurred");
        assertThat(problemDetail.getInstance()).isEqualTo(URI.create("/internal/api/v1/customers"));
        assertThat(problemDetail.getProperties()).isNotNull();
        assertThat(problemDetail.getProperties().containsKey("errorId")).isTrue();
    }

}
