/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright (C) 2026-present Gecko Solutions OÜ
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ee.geckosolutions.mra.gateway.test;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TestUtil {

    private static final JwtEncoder JWT_ENCODER = NimbusJwtEncoder.withSecretKey(resolveJwtSecretKey())
            .algorithm(MacAlgorithm.HS256)
            .build();

    public static SecretKey resolveJwtSecretKey() {
        byte[] keyBytes = "a-string-secret-at-least-256-bits-long".getBytes(StandardCharsets.UTF_8);
        return new SecretKeySpec(keyBytes, "HmacSHA256");
    }

    public static String getToken() {
        return JWT_ENCODER.encode(resolveJwtEncoderParameters()).getTokenValue();
    }

    private static JwtEncoderParameters resolveJwtEncoderParameters() {
        Instant now = Instant.now();
        JwtClaimsSet jwtClaimsSet = JwtClaimsSet.builder()
                .issuer("https://kc.mra.local:9443/realms/mra")
                .subject("04f47c28-9097-455e-806c-1ebc6a6295ed")
                .audience(List.of("mra"))
                .issuedAt(now)
                .expiresAt(now.plus(Duration.ofHours(1)))
                .claim("scope", "email profile")
                .build();
        return JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), jwtClaimsSet);
    }

}
