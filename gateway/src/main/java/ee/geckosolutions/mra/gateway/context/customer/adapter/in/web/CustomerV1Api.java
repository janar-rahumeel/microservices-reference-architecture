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

import java.util.UUID;

import ee.geckosolutions.mra.common.contract.customer.web.dto.CustomerV1;
import ee.geckosolutions.mra.common.contract.customer.web.dto.NewCustomerV1;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Customers API", description = "Customer management operations")
public interface CustomerV1Api {

    @Operation(
            summary = "Get customer by ID",
            description = "Returns a single customer. **This endpoint is deprecated.** Use /api/v2/customers/{id} endpoint instead",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Customer found",
                            content = @Content(schema = @Schema(implementation = CustomerV1.class))),
                    @ApiResponse(responseCode = "404", description = "Customer not found") })
    @Deprecated
    ResponseEntity<byte[]> get(UUID id);

    @Operation(
            summary = "Create a new customer",
            description = "Creates a new customer. **This endpoint is deprecated.** Use /api/v2/customers endpoint instead",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(schema = @Schema(implementation = NewCustomerV1.class))),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Customer created",
                            content = @Content(schema = @Schema(implementation = CustomerV1.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid input"),
                    @ApiResponse(responseCode = "409", description = "Customer already exists") })
    @Deprecated
    ResponseEntity<byte[]> insert(@RequestBody byte[] content);

}
