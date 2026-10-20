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
package ee.geckosolutions.mra.common.contract.access.web.dto;

import java.util.Set;
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
public class UserV1Response {

    @Schema(
            requiredMode = Schema.RequiredMode.REQUIRED,
            description = "Unique identifier of the user, equal to the identity provider subject",
            example = "3fa85f64-5717-4562-b3fc-2c963f66afa5")
    private final UUID id;

    @Schema(
            requiredMode = Schema.RequiredMode.REQUIRED,
            description = "Authorities assigned to the user",
            example = "[\"ROLE_USER\", \"PERMISSION_CUSTOMER_READ\"]")
    @Builder.Default
    private final Set<String> authorities = Set.of();

}
