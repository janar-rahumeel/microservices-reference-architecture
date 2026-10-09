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
package ee.geckosolutions.mra.common.domain.identifier;

import java.security.SecureRandom;
import java.util.UUID;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class UuidV7 {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    public static UUID generate() {
        long timestamp = System.currentTimeMillis();
        long randomHigh = SECURE_RANDOM.nextLong();
        long randomLow = SECURE_RANDOM.nextLong();
        long mostSignificantBits = (timestamp << 16) | 0x7000L | (randomHigh & 0x0FFFL);
        long leastSignificantBits = (randomLow & 0x3FFFFFFFFFFFFFFFL) | 0x8000000000000000L;
        return new UUID(mostSignificantBits, leastSignificantBits);
    }

}
