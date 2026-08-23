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
package ee.geckosolutions.mra.common.contract.customer.web.dto;

import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.jackson.Jacksonized;

@Setter
@Getter
@Builder
@Jacksonized
public class CustomerSearchElementV2Response {

    @Schema(description = "Customer identifier", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private final UUID id;

    @Schema(description = "Customer type", example = "PERSON")
    private final CustomerTypeV2 type;

    @Schema(description = "Legal entity name or person's full name", example = "Chuck Norris")
    private final String name;

    @Schema(description = "Either registration or personal identification code", example = "38208273384")
    private final String code;

}
