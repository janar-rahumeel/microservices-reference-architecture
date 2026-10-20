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
package ee.geckosolutions.mra.common.platform.exception;

import lombok.Getter;

@Getter
public abstract class AbstractApplicationException extends RuntimeException implements ErrorDescriptor {

    private final ApplicationExceptionType type;

    private final String errorCode;

    protected AbstractApplicationException(ApplicationExceptionType type, String errorCode, String message) {
        super(message);
        this.type = type;
        this.errorCode = errorCode;
    }

    protected AbstractApplicationException(ApplicationExceptionType type, String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.type = type;
        this.errorCode = errorCode;
    }

}
