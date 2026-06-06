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
package ee.geckosolutions.mra.gateway.context.customer.adapter.in.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import ee.geckosolutions.mra.common.contract.customer.web.dto.CustomerTypeV1;
import ee.geckosolutions.mra.common.contract.customer.web.dto.CustomerV1;
import ee.geckosolutions.mra.common.contract.customer.web.dto.NewCustomerV1;
import ee.geckosolutions.mra.gateway.test.AbstractWebIntegrationTest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

class CustomerV1ControllerIntegrationTest extends AbstractWebIntegrationTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testThatGetCustomerV1IsSuccessful() {
        // given
        String customerId = UUID.randomUUID().toString();
        String firstName = "Chuck";
        String lastName = "Norris";
        String personalIdentificationCode = "38208274481";

        URI uri = URI.create("http://core.test/internal/api/v1/customers/" + customerId);
        coreServiceMockRestServiceServer.expect(requestTo(uri))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE))
                .andRespond(
                        withSuccess(
                                "{\"id\":\"" + customerId + "\",\"type\":\"PRIVATE\",\"firstName\":\"" + firstName
                                        + "\",\"lastName\":\"" + lastName + "\",\"personalIdentificationCode\":\""
                                        + personalIdentificationCode + "\"}",
                                MediaType.APPLICATION_JSON));

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setBearerAuth("token");
        httpHeaders.setAccept(List.of(MediaType.APPLICATION_JSON));
        HttpEntity<String> httpEntity = new HttpEntity<>(httpHeaders);

        // when
        ResponseEntity<CustomerV1> responseEntity = testRestTemplate
                .exchange("/api/v1/customers/" + customerId, HttpMethod.GET, httpEntity, CustomerV1.class);

        // then
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);

        CustomerV1 customerV1 = responseEntity.getBody();
        assertThat(customerV1).isNotNull();
        assertThat(customerV1.getId()).isEqualTo(UUID.fromString(customerId));
        assertThat(customerV1.getType()).isEqualTo(CustomerTypeV1.PRIVATE);
        assertThat(customerV1.getFirstName()).isEqualTo(firstName);
        assertThat(customerV1.getLastName()).isEqualTo(lastName);
        assertThat(customerV1.getPersonalIdentificationCode()).isEqualTo(personalIdentificationCode);
        assertThat(customerV1.getName()).isNull();
        assertThat(customerV1.getRegistrationCode()).isNull();

        HttpHeaders responseEntityHeaders = responseEntity.getHeaders();
        assertThat(responseEntityHeaders.containsHeaderValue("deprecation", "@4070908800")).isTrue();
        assertThat(responseEntityHeaders.containsHeaderValue("sunset", "Wed, 1 Jul 2099 00:00:00 GMT")).isTrue();

        coreServiceMockRestServiceServer.verify();
    }

    @Test
    void testThatInsertCustomerV1IsSuccessful() {
        // given
        String customerId = UUID.randomUUID().toString();
        String name = "Chuck Norris Ltd.";
        String registrationCode = "US656734";

        URI uri = URI.create("http://core.test/internal/api/v1/customers");
        coreServiceMockRestServiceServer.expect(requestTo(uri))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE))
                .andExpect(header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
                .andRespond(
                        withSuccess(
                                "{\"id\":\"" + customerId + "\",\"type\":\"COMPANY\",\"name\":\"" + name
                                        + "\",\"registrationCode\":\"" + registrationCode + "\"}",
                                MediaType.APPLICATION_JSON));

        NewCustomerV1 newCustomerV1 = NewCustomerV1.builder()
                .type(CustomerTypeV1.COMPANY)
                .name(name)
                .registrationCode(registrationCode)
                .build();
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setBearerAuth("token");
        httpHeaders.setAccept(List.of(MediaType.APPLICATION_JSON));
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<NewCustomerV1> httpEntity = new HttpEntity<>(newCustomerV1, httpHeaders);

        // when
        ResponseEntity<CustomerV1> responseEntity = testRestTemplate
                .exchange("/api/v1/customers", HttpMethod.POST, httpEntity, CustomerV1.class);

        // then
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);

        CustomerV1 customerV1 = responseEntity.getBody();
        assertThat(customerV1).isNotNull();
        assertThat(customerV1.getId()).isEqualTo(UUID.fromString(customerId));
        assertThat(customerV1.getName()).isEqualTo(name);
        assertThat(customerV1.getRegistrationCode()).isEqualTo(registrationCode);

        coreServiceMockRestServiceServer.verify();
    }

    @Test
    void testThatGetCustomerV1NotFoundReturnsCorrectErrorResponse() {
        // given
        String customerId = UUID.randomUUID().toString();
        String errorId = UUID.randomUUID().toString();
        String errorMessage = "Customer not found";
        String errorResponseJson = """
                {"id":"%s","errorCode":"general.technical.entity-not-found","message":"%s"}
                """.formatted(errorId, errorMessage);

        URI uri = URI.create("http://core.test/internal/api/v1/customers/" + customerId);
        coreServiceMockRestServiceServer.expect(requestTo(uri))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE))
                .andRespond(withStatus(HttpStatus.NOT_FOUND).contentType(MediaType.APPLICATION_JSON).body(errorResponseJson));

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setBearerAuth("token");
        httpHeaders.setAccept(List.of(MediaType.APPLICATION_JSON));
        HttpEntity<String> httpEntity = new HttpEntity<>(httpHeaders);

        // when
        ResponseEntity<String> responseEntity = testRestTemplate
                .exchange("/api/v1/customers/" + customerId, HttpMethod.GET, httpEntity, String.class);

        // then
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        String responseBody = responseEntity.getBody();
        assertThat(responseBody).isNotNull();

        JsonNode responseJson = objectMapper.readTree(responseBody);
        assertThat(responseJson).isEqualTo(objectMapper.readTree("""
                {"id":"%s","errorCode":"general.technical.entity-not-found","message":"%s"}
                """.formatted(errorId, errorMessage)));

        HttpHeaders responseEntityHeaders = responseEntity.getHeaders();
        assertThat(responseEntityHeaders.containsHeaderValue("deprecation", "@4070908800")).isTrue();
        assertThat(responseEntityHeaders.containsHeaderValue("sunset", "Wed, 1 Jul 2099 00:00:00 GMT")).isTrue();

        coreServiceMockRestServiceServer.verify();
    }

    @Test
    void testThatGetCustomerV1WithoutBearerAuthReturnsStandardErrorResponse() {
        // given
        String customerId = UUID.randomUUID().toString();

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setAccept(List.of(MediaType.APPLICATION_JSON));
        HttpEntity<String> httpEntity = new HttpEntity<>(httpHeaders);

        // when
        ResponseEntity<String> responseEntity = testRestTemplate
                .exchange("/api/v1/customers/" + customerId, HttpMethod.GET, httpEntity, String.class);

        // then
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(responseEntity.getHeaders().getFirst(HttpHeaders.WWW_AUTHENTICATE)).matches(
                "Bearer realm=\"mra\", resource_metadata=\"http://localhost:\\d+/.well-known/oauth-protected-resource\"");
        assertThat(responseEntity.getBody()).isNull();
    }

}
