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

import java.util.List;
import java.util.stream.Collectors;

import ee.geckosolutions.mra.common.domain.exception.AbstractDomainException;
import ee.geckosolutions.mra.common.domain.exception.DomainExceptionType;

import lombok.Getter;

@Getter
public final class InvariantViolationException extends AbstractDomainException {

    private static final String ERROR_CODE = "general.business.invariant-violation";

    private final List<FieldViolation> violations;

    public InvariantViolationException(List<FieldViolation> violations) {
        super(DomainExceptionType.INVARIANT_VIOLATION, ERROR_CODE, describe(violations));
        this.violations = List.copyOf(violations);
    }

    private static String describe(List<FieldViolation> violations) {
        return violations.stream()
                .map(violation -> violation.field() + ": " + violation.code())
                .collect(Collectors.joining(", ", "Invariant violation(s): ", ""));
    }

}
