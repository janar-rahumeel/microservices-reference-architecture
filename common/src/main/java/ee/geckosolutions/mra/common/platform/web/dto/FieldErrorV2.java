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

import java.util.Map;

import ee.geckosolutions.mra.common.platform.validation.ErrorCode;
import ee.geckosolutions.mra.common.platform.web.jackson.ErrorCodeSerializer;

import com.fasterxml.jackson.annotation.JsonInclude;
import tools.jackson.databind.annotation.JsonSerialize;

public record FieldErrorV2(String field, @JsonSerialize(using = ErrorCodeSerializer.class) ErrorCode code,
        @JsonInclude(JsonInclude.Include.NON_EMPTY) Map<String, Object> parameters) {
}
