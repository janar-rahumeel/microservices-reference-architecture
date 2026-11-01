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
package ee.geckosolutions.mra.common.platform.web.security;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode(of = "value")
public final class Sha256Hash {

    private final byte[] value;

    public Sha256Hash(byte[] hash) {
        if (hash.length != 32) {
            throw new IllegalArgumentException("SHA-256 hash must contain exactly 32 bytes");
        }

        this.value = hash.clone();
    }

    public byte[] getValue() {
        return value.clone();
    }

}
