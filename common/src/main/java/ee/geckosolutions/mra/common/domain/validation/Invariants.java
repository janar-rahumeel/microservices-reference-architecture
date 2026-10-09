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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.jspecify.annotations.Nullable;

public final class Invariants {

    public static final String NOT_NULL = "validation.not-null";
    public static final String NOT_BLANK = "validation.not-blank";
    public static final String NOT_EMPTY = "validation.not-empty";
    public static final String TOO_SHORT = "validation.too-short";
    public static final String TOO_LONG = "validation.too-long";
    public static final String EXACT_DIGITS = "validation.exact-digits";

    private final List<FieldViolation> violations = new ArrayList<>();

    private Invariants() {
    }

    public static Invariants check() {
        return new Invariants();
    }

    public Invariants notNull(String field, @Nullable Object value) {
        return that(value != null, field, NOT_NULL);
    }

    public Invariants notBlank(String field, @Nullable String value) {
        return that(value != null && !value.isBlank(), field, NOT_BLANK);
    }

    public Invariants notEmpty(String field, @Nullable Collection<?> value) {
        return that(value != null && !value.isEmpty(), field, NOT_EMPTY);
    }

    public Invariants minLength(String field, @Nullable String value, int min) {
        return that(value == null || value.length() >= min, field, TOO_SHORT, Map.of("min", min));
    }

    public Invariants maxLength(String field, @Nullable String value, int max) {
        return that(value == null || value.length() <= max, field, TOO_LONG, Map.of("max", max));
    }

    public Invariants exactDigits(String field, @Nullable String value, int length) {
        return that(
                value == null || value.isBlank() || isExactDigits(value, length),
                field,
                EXACT_DIGITS,
                Map.of("length", length));
    }

    private static boolean isExactDigits(String value, int length) {
        return value.length() == length && value.chars().allMatch(character -> character >= '0' && character <= '9');
    }

    public Invariants that(boolean valid, String field, String code) {
        return that(valid, field, code, Map.of());
    }

    public Invariants that(boolean valid, String field, String code, Map<String, Object> params) {
        if (!valid) {
            violations.add(new FieldViolation(field, code, params));
        }
        return this;
    }

    public void throwIfViolated() {
        if (!violations.isEmpty()) {
            throw new InvariantViolationException(violations);
        }
    }

}
