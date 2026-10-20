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
package ee.geckosolutions.mra.common.platform.web.dto;

import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;

public record ErrorResponseV2(UUID id, String errorCode, String message,
        @JsonInclude(JsonInclude.Include.NON_EMPTY) List<FieldErrorV2> fieldErrors) {

    public ErrorResponseV2(UUID id, String errorCode, String message) {
        this(id, errorCode, message, List.of());
    }

}
