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
import java.util.concurrent.Callable;

import ee.geckosolutions.mra.common.contract.customer.web.dto.CustomerSearchPageV2Response;
import ee.geckosolutions.mra.common.contract.customer.web.dto.LegalEntityCustomerV2;
import ee.geckosolutions.mra.common.contract.customer.web.dto.NewLegalEntityCustomerV2;
import ee.geckosolutions.mra.common.contract.customer.web.dto.NewPersonCustomerV2;
import ee.geckosolutions.mra.common.contract.customer.web.dto.PersonCustomerV2;
import ee.geckosolutions.mra.common.platform.web.dto.ErrorResponseV2;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;

@Tag(name = "Customers API", description = "Customer management operations")
public interface CustomerV2Api {

    @Operation(
            summary = "Search customers by filter",
            description = "Returns customers matching the specified filter",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Customers found",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = CustomerSearchPageV2Response.class))) })
    @Parameters({
            @Parameter(
                    name = "page",
                    in = ParameterIn.QUERY,
                    description = "Zero-based page index",
                    schema = @Schema(type = "integer", defaultValue = "0", minimum = "0", example = "0")),
            @Parameter(
                    name = "size",
                    in = ParameterIn.QUERY,
                    description = "Number of items per page",
                    schema = @Schema(type = "integer", defaultValue = "20", minimum = "1", example = "20")),
            @Parameter(
                    name = "sort",
                    in = ParameterIn.QUERY,
                    description = "Sorting criteria. Repeat for multiple properties",
                    schema = @Schema(type = "string", example = "todo,desc")) })
    Callable<ResponseEntity<byte[]>> search(MultiValueMap<String, String> parameters);

    @Operation(
            summary = "Create a new customer",
            description = "Creates a new customer",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            schema = @Schema(oneOf = { NewPersonCustomerV2.class, NewLegalEntityCustomerV2.class }))),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Customer created",
                            content = @Content(
                                    schema = @Schema(oneOf = { PersonCustomerV2.class, LegalEntityCustomerV2.class }))),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid input",
                            content = @Content(schema = @Schema(implementation = ErrorResponseV2.class))),
                    @ApiResponse(
                            responseCode = "409",
                            description = "Customer already exists",
                            content = @Content(schema = @Schema(implementation = ErrorResponseV2.class))) })
    Callable<ResponseEntity<byte[]>> insert(byte[] content);

    @Operation(
            summary = "Get customer by ID",
            description = "Returns a single customer",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Customer found",
                            content = @Content(
                                    schema = @Schema(oneOf = { PersonCustomerV2.class, LegalEntityCustomerV2.class }))),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Customer not found",
                            content = @Content(schema = @Schema(implementation = ErrorResponseV2.class))) })
    Callable<ResponseEntity<byte[]>> get(UUID id);

}
