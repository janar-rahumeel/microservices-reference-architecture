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
package ee.geckosolutions.mra.core.context.customer.adapter.out.messaging;

import ee.geckosolutions.mra.common.context.customer.adapter.CustomerRabbitContract;
import ee.geckosolutions.mra.common.platform.observation.Adapter;
import ee.geckosolutions.mra.common.platform.observation.AdapterDirection;
import ee.geckosolutions.mra.common.platform.observation.AdapterType;
import ee.geckosolutions.mra.common.platform.observation.BoundedContext;
import ee.geckosolutions.mra.core.context.customer.application.port.CustomerEventPublisher;
import ee.geckosolutions.mra.core.context.customer.domain.event.CustomerCreatedEvent;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Adapter(direction = AdapterDirection.OUT, type = AdapterType.RABBITMQ, boundedContext = BoundedContext.CUSTOMER)
@Component
@RequiredArgsConstructor
public class RabbitCustomerEventPublisher implements CustomerEventPublisher {

    private final CustomerEventMapper customerEventMapper;
    private final RabbitTemplate rabbitTemplate;

    public void publish(CustomerCreatedEvent customerCreatedEvent) {
        rabbitTemplate.convertAndSend(
                CustomerRabbitContract.CUSTOMER_EXCHANGE_NAME,
                CustomerRabbitContract.RoutingKeys.CUSTOMER_CREATED_EVENT,
                customerEventMapper.toCustomerCreatedEventV1(customerCreatedEvent));
    }

}
