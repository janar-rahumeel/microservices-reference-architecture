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

import ee.geckosolutions.mra.common.contract.customer.messaging.dto.CustomerCreatedEventV1;
import ee.geckosolutions.mra.worker.context.customer.adapter.in.messaging.config.CustomerRabbitConfiguration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CustomerEventListener {

    @RabbitListener(queues = CustomerRabbitConfiguration.EVENTS_QUEUE_NAME)
    public void onCustomerCreated(@Header(value = "traceparent") String traceparent, CustomerCreatedEventV1 event) {
        log.info("[{}] Do something reasonable when a new customer has been created - {}", traceparent, event.id());
    }

}
