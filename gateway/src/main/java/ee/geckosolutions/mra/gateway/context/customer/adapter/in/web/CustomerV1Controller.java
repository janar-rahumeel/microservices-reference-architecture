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

import java.time.Instant;
import java.util.UUID;

import ee.geckosolutions.mra.gateway.adapter.in.web.ControllerSupport;
import ee.geckosolutions.mra.gateway.config.ApplicationProperties;
import ee.geckosolutions.mra.gateway.context.customer.adapter.out.api.InternalCustomerV1Client;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/v1/customers")
@RequiredArgsConstructor
public class CustomerV1Controller implements CustomerV1Api {

    private final ApplicationProperties applicationProperties;
    private final InternalCustomerV1Client internalCustomerV1Client;
    private final ControllerSupport controllerSupport;

    @Override
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<byte[]> get(@PathVariable UUID id) {
        Instant v1DeprecatedSince = applicationProperties.getCustomer().getV1ApiDeprecatedSince();
        return controllerSupport.forwardWithDeprecationHeaders(internalCustomerV1Client.get(id), v1DeprecatedSince);
    }

    @Override
    @PostMapping(value = "", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<byte[]> insert(@RequestBody byte[] content) {
        Instant v1DeprecatedSince = applicationProperties.getCustomer().getV1ApiDeprecatedSince();
        return controllerSupport.forwardWithDeprecationHeaders(internalCustomerV1Client.insert(content), v1DeprecatedSince);
    }

}
