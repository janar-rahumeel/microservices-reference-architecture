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
package ee.geckosolutions.mra.common.domain.validation;

import java.util.Map;

public record FieldViolation(String field, String code, Map<String, Object> parameters) {

    public FieldViolation {
        parameters = Map.copyOf(parameters);
    }

    public FieldViolation(String field, String code) {
        this(field, code, Map.of());
    }

}
