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
package ee.geckosolutions.mra.common.platform.web.jackson;

import ee.geckosolutions.mra.common.platform.validation.ErrorCode;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;

public class ErrorCodeSerializer extends ValueSerializer<ErrorCode> {

    @Override
    public void serialize(ErrorCode errorCode, JsonGenerator jsonGenerator, SerializationContext serializationContext)
            throws JacksonException {
        jsonGenerator.writeString(errorCode.getValue());
    }

}
