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
package ee.geckosolutions.mra.common.platform.observation.ecs;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.StandardClaimNames;
import org.springframework.security.oauth2.server.resource.authentication.AbstractOAuth2TokenAuthenticationToken;

public class OAuth2UserEcsLoggingContextContributor implements EcsLoggingContextContributor {

    private static final String USER_ID = "ecs:user.id";
    private static final String USER_NAME = "ecs:user.name";
    private static final String USER_ROLES = "ecs:user.roles";
    private static final String ROLE_PREFIX = "ROLE_";

    @Override
    public void contribute(Map<String, String> context) {
        context.remove(USER_ID);
        context.remove(USER_NAME);
        context.remove(USER_ROLES);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication instanceof AbstractOAuth2TokenAuthenticationToken<?> token && token.isAuthenticated()) {
            context.put(USER_NAME, token.getName());
            Map<String, Object> claims = token.getTokenAttributes();
            Optional.ofNullable(claims.get(StandardClaimNames.SUB)).ifPresent(id -> context.put(USER_ID, "" + id));
            resolveRoles(token).ifPresent(roles -> context.put(USER_ROLES, roles));
        }
    }

    private static Optional<String> resolveRoles(Authentication authentication) {
        return Optional
                .of(
                        authentication.getAuthorities()
                                .stream()
                                .map(GrantedAuthority::getAuthority)
                                .filter(authority -> authority != null && authority.startsWith(ROLE_PREFIX))
                                .map(authority -> authority.substring(ROLE_PREFIX.length()))
                                .distinct()
                                .collect(Collectors.joining(EcsStructuredLogFormatter.MULTI_VALUE_SEPARATOR)))
                .filter(roles -> !roles.isEmpty());
    }

}
