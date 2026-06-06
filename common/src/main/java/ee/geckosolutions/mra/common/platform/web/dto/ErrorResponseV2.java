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

import java.util.UUID;

import ee.geckosolutions.mra.common.platform.validation.ErrorCode;
import ee.geckosolutions.mra.common.platform.web.jackson.ErrorCodeSerializer;

import tools.jackson.databind.annotation.JsonSerialize;

public record ErrorResponseV2(UUID id, @JsonSerialize(using = ErrorCodeSerializer.class) ErrorCode errorCode, String message) {
}
