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
package ee.geckosolutions.mra.worker.context.customer.adapter.in.messaging.config;

import ee.geckosolutions.mra.common.context.customer.adapter.CustomerRabbitContract;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class CustomerRabbitConfiguration {

    public static final String EVENTS_QUEUE_NAME = "mra.worker.customer.events";

    @Bean
    Queue customerEventsQueue() {
        return QueueBuilder.durable(EVENTS_QUEUE_NAME).quorum().build();
    }

    @Bean
    TopicExchange customerExchange() {
        return new TopicExchange(CustomerRabbitContract.CUSTOMER_EXCHANGE_NAME);
    }

    @Bean
    Binding customerEventsBinding(Queue customerEventsQueue, TopicExchange customerExchange) {
        return BindingBuilder.bind(customerEventsQueue)
                .to(customerExchange)
                .with(CustomerRabbitContract.RoutingKeys.CUSTOMER_CREATED_EVENT);
    }

}
