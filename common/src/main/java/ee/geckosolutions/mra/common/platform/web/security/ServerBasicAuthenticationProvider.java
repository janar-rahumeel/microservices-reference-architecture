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

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

@Slf4j
public class ServerBasicAuthenticationProvider implements AuthenticationProvider {

    private static final byte[] UNKNOWN_USERNAME_HASH = new byte[32];

    private final Map<String, ServerCredential> credentials;

    public ServerBasicAuthenticationProvider(List<? extends ServerCredential> serverCredentials) {
        this.credentials = serverCredentials.stream()
                .collect(Collectors.toUnmodifiableMap(ServerCredential::getUsername, Function.identity()));
    }

    @Override
    public Authentication authenticate(Authentication authentication) {
        String username = authentication.getName();
        byte[] expectedPasswordHash = Optional.ofNullable(credentials.get(username))
                .map(credentials -> credentials.getPassword().getValue())
                .orElse(UNKNOWN_USERNAME_HASH);

        if (!(authentication.getCredentials() instanceof String password) || !matches(password, expectedPasswordHash)) {
            throw new BadCredentialsException("Invalid credentials");
        }

        return UsernamePasswordAuthenticationToken.authenticated(username, null, Set.of());
    }

    public static boolean matches(String password, byte[] expectedPasswordHash) {
        try {
            byte[] actualPasswordHash = MessageDigest.getInstance("SHA-256").digest(password.getBytes(StandardCharsets.UTF_8));
            return MessageDigest.isEqual(actualPasswordHash, expectedPasswordHash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }

}
