/*
 * SPDX-License-Identifier: NONE
 *
 * Copyright (C) 2026-present Gecko Solutions OÜ
 * All rights reserved.
 *
 * This software is the proprietary and confidential property of Gecko Solutions OÜ.
 * Unauthorized copying, redistribution, or modification of this file, in whole or in part,
 * is strictly prohibited without prior written consent from Gecko Solutions OÜ.
 *
 * For licensing information, contact: licensing@geckosolutions.ee
 */
package ee.geckosolutions.mra.common.context.customer.adapter.out.api;

import java.util.UUID;

import ee.geckosolutions.mra.common.contract.customer.web.dto.CustomerV1;
import ee.geckosolutions.mra.common.contract.customer.web.dto.NewCustomerV1;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

@HttpExchange(url = "/internal/api/v1/customers")
public interface InternalCustomerV1Client {

    @Deprecated
    @GetExchange(url = "/{id}", accept = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<CustomerV1> get(@PathVariable UUID id);

    @Deprecated
    @PostExchange(url = "/", contentType = MediaType.APPLICATION_JSON_VALUE, accept = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<CustomerV1> insert(@RequestBody NewCustomerV1 newCustomerV1);

}
