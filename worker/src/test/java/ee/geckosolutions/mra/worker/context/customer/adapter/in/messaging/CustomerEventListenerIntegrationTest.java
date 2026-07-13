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
package ee.geckosolutions.mra.worker.context.customer.adapter.in.messaging;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.verify;

import java.time.Duration;
import java.util.UUID;

import ee.geckosolutions.mra.common.context.customer.adapter.CustomerRabbitContract;
import ee.geckosolutions.mra.common.contract.customer.messaging.dto.CustomerCreatedEventV1;
import ee.geckosolutions.mra.worker.test.AbstractIntegrationTest;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

@TestPropertySource(properties = "spring.rabbitmq.template.observation-enabled=false")
class CustomerEventListenerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @MockitoSpyBean
    private CustomerEventListener customerEventListener;

    @Test
    void testThatOnCustomerCreatedIsSuccessful() {
        // given
        UUID customerId = UUID.randomUUID();
        CustomerCreatedEventV1 customerCreatedEventV1 = new CustomerCreatedEventV1(customerId);
        String traceparent = "00-4bf92f3577b34da6a3ce929d0e0e4736-00f066aa0ba902b9-01";

        // when
        rabbitTemplate.convertAndSend(
                CustomerRabbitContract.CUSTOMER_EXCHANGE_NAME,
                CustomerRabbitContract.RoutingKeys.CUSTOMER_CREATED_EVENT,
                customerCreatedEventV1,
                message -> {
                    message.getMessageProperties().setHeader("traceparent", traceparent);
                    return message;
                });

        // then
        ArgumentCaptor<String> traceparentCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<CustomerCreatedEventV1> eventCaptor = ArgumentCaptor.forClass(CustomerCreatedEventV1.class);
        await().atMost(Duration.ofSeconds(5))
                .untilAsserted(
                        () -> verify(customerEventListener)
                                .onCustomerCreated(traceparentCaptor.capture(), eventCaptor.capture()));
        assertThat(traceparentCaptor.getValue()).isEqualTo(traceparent);
        assertThat(eventCaptor.getValue().id()).isEqualTo(customerId);
    }

}
