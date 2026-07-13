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
package ee.geckosolutions.mra.core.test;

import ee.geckosolutions.mra.common.platform.validation.ErrorCode;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.rabbitmq.RabbitMQContainer;
import tools.jackson.databind.JacksonModule;
import tools.jackson.databind.module.SimpleModule;

@TestConfiguration(proxyBeanMethods = false)
public class IntegrationTestConfiguration {

    public static final String EVENTS_QUEUE_NAME = "test.events";

    @Bean
    @ServiceConnection
    PostgreSQLContainer postgreSqlContainer() {
        return new PostgreSQLContainer("postgres:18.4-alpine");
    }

    @Bean
    @ServiceConnection
    RabbitMQContainer rabbitMqContainer() {
        return new RabbitMQContainer("rabbitmq:4.3-alpine");
    }

    @Bean
    JacksonModule jacksonModule() {
        SimpleModule simpleModule = new SimpleModule();
        simpleModule.addDeserializer(ErrorCode.class, new ErrorCodeDeserializer());
        return simpleModule;
    }

    @Bean
    Queue eventsQueue() {
        return QueueBuilder.durable(EVENTS_QUEUE_NAME).quorum().build();
    }

    @Bean
    Binding eventsBinding(Queue eventsQueue, TopicExchange customerExchange) {
        return BindingBuilder.bind(eventsQueue).to(customerExchange).with("event.#");
    }

}
