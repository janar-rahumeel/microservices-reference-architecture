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
import static org.springframework.test.web.client.response.MockRestResponseCreators.withException;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.net.ConnectException;
import java.net.URI;
import java.util.List;
import java.util.UUID;

import ee.geckosolutions.mra.common.contract.customer.web.dto.LegalEntityCustomerV2;
import ee.geckosolutions.mra.common.contract.customer.web.dto.NewLegalEntityCustomerV2;
import ee.geckosolutions.mra.common.contract.customer.web.dto.PersonCustomerV2;
import ee.geckosolutions.mra.gateway.test.AbstractWebIntegrationTest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

class CustomerV2ControllerIntegrationTest extends AbstractWebIntegrationTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Test
    void testThatGetCustomerV2IsSuccessful() {
        // given
        String customerId = UUID.randomUUID().toString();
        String firstName = "Chuck";
        String lastName = "Norris";
        String personalIdentificationCode = "38208273384";

        URI uri = URI.create("http://core.test/internal/api/v2/customers/" + customerId);
        coreServiceMockRestServiceServer.expect(requestTo(uri))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE))
                .andRespond(
                        withSuccess(
                                "{\"id\":\"" + customerId + "\",\"type\":\"PERSON\",\"firstName\":\"" + firstName
                                        + "\",\"lastName\":\"" + lastName + "\",\"personalIdentificationCode\":\""
                                        + personalIdentificationCode + "\"}",
                                MediaType.APPLICATION_JSON));

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setAccept(List.of(MediaType.APPLICATION_JSON));
        HttpEntity<String> httpEntity = new HttpEntity<>(httpHeaders);

        // when
        ResponseEntity<PersonCustomerV2> responseEntity = testRestTemplate
                .exchange("/api/v2/customers/" + customerId, HttpMethod.GET, httpEntity, PersonCustomerV2.class);

        // then
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);

        PersonCustomerV2 personCustomerV2 = responseEntity.getBody();
        assertThat(personCustomerV2).isNotNull();
        assertThat(personCustomerV2.getId()).isEqualTo(UUID.fromString(customerId));
        assertThat(personCustomerV2.getFirstName()).isEqualTo(firstName);
        assertThat(personCustomerV2.getLastName()).isEqualTo(lastName);
        assertThat(personCustomerV2.getPersonalIdentificationCode()).isEqualTo(personalIdentificationCode);

        coreServiceMockRestServiceServer.verify();
    }

    @Test
    void testThatInsertCustomerV2IsSuccessful() {
        // given
        String customerId = UUID.randomUUID().toString();
        String name = "Chuck Norris Ltd.";
        String registrationCode = "US656799";

        URI uri = URI.create("http://core.test/internal/api/v2/customers");
        coreServiceMockRestServiceServer.expect(requestTo(uri))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE))
                .andExpect(header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
                .andRespond(
                        withSuccess(
                                "{\"id\":\"" + customerId + "\",\"type\":\"LEGAL_ENTITY\",\"name\":\"" + name
                                        + "\",\"registrationCode\":\"" + registrationCode + "\"}",
                                MediaType.APPLICATION_JSON));

        NewLegalEntityCustomerV2 newLegalEntityCustomerV2 = NewLegalEntityCustomerV2.builder()
                .name(name)
                .registrationCode(registrationCode)
                .build();
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setAccept(List.of(MediaType.APPLICATION_JSON));
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<NewLegalEntityCustomerV2> httpEntity = new HttpEntity<>(newLegalEntityCustomerV2, httpHeaders);

        // when
        ResponseEntity<LegalEntityCustomerV2> responseEntity = testRestTemplate
                .exchange("/api/v2/customers", HttpMethod.POST, httpEntity, LegalEntityCustomerV2.class);

        // then
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);

        LegalEntityCustomerV2 legalEntityCustomerV2 = responseEntity.getBody();
        assertThat(legalEntityCustomerV2).isNotNull();
        assertThat(legalEntityCustomerV2.getId()).isEqualTo(UUID.fromString(customerId));
        assertThat(legalEntityCustomerV2.getName()).isEqualTo(name);
        assertThat(legalEntityCustomerV2.getRegistrationCode()).isEqualTo(registrationCode);

        coreServiceMockRestServiceServer.verify();
    }

    @Test
    void testThatInsertCustomerV2BadRequestReturnsErrorResponseJson() throws Exception {
        // given
        String errorId = UUID.randomUUID().toString();
        String errorMessage = "Invalid customer payload";
        String errorResponseJson = """
                {"id":"%s","errorCode":{"value":"general.technical.input-validation"},"message":"%s"}
                """.formatted(errorId, errorMessage);

        URI uri = URI.create("http://core.test/internal/api/v2/customers");
        coreServiceMockRestServiceServer.expect(requestTo(uri))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE))
                .andExpect(header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
                .andRespond(withStatus(HttpStatus.BAD_REQUEST).contentType(MediaType.APPLICATION_JSON).body(errorResponseJson));

        NewLegalEntityCustomerV2 invalidPayload = NewLegalEntityCustomerV2.builder().name("").registrationCode("").build();
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setAccept(List.of(MediaType.APPLICATION_JSON));
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<NewLegalEntityCustomerV2> httpEntity = new HttpEntity<>(invalidPayload, httpHeaders);

        // when
        ResponseEntity<String> responseEntity = testRestTemplate
                .exchange("/api/v2/customers", HttpMethod.POST, httpEntity, String.class);

        // then
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        String responseBody = responseEntity.getBody();
        assertThat(responseBody).isNotNull();

        JsonNode responseJson = OBJECT_MAPPER.readTree(responseBody);
        assertThat(responseJson.path("id").asText()).isEqualTo(errorId);
        assertThat(responseJson.path("errorCode").path("value").asText()).isEqualTo("general.technical.input-validation");
        assertThat(responseJson.path("message").asText()).isEqualTo(errorMessage);

        coreServiceMockRestServiceServer.verify();
    }

    @Test
    void testThatInsertCustomerV2ConnectTimeoutReturnsServiceUnavailableErrorResponseJson() throws Exception {
        // given
        URI uri = URI.create("http://core.test/internal/api/v2/customers");
        coreServiceMockRestServiceServer.expect(requestTo(uri))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE))
                .andExpect(header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
                .andRespond(withException(new ConnectException("Connect timed out")));

        NewLegalEntityCustomerV2 validPayload = NewLegalEntityCustomerV2.builder()
                .name("Timeout Ltd.")
                .registrationCode("US656701")
                .build();
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setAccept(List.of(MediaType.APPLICATION_JSON));
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<NewLegalEntityCustomerV2> httpEntity = new HttpEntity<>(validPayload, httpHeaders);

        // when
        ResponseEntity<String> responseEntity = testRestTemplate
                .exchange("/api/v2/customers", HttpMethod.POST, httpEntity, String.class);

        // then
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);

        String responseBody = responseEntity.getBody();
        assertThat(responseBody).isNotNull();

        JsonNode responseJson = OBJECT_MAPPER.readTree(responseBody);
        assertThat(responseJson.path("id").asText()).isNotBlank();
        assertThat(responseJson.path("errorCode").path("value").asText()).isEqualTo("general.technical.service-unavailable");
        assertThat(responseJson.path("message").asText()).isEqualTo("Downstream service is unavailable");

        coreServiceMockRestServiceServer.verify();
    }

}
