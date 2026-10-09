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

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@RequiredArgsConstructor
public class EcsLoggingContextFilter extends OncePerRequestFilter {

    private final List<EcsLoggingContextContributor> contributors;

    @Override
    protected void doFilterInternal(
            HttpServletRequest servletRequest,
            HttpServletResponse servletResponse,
            FilterChain filterChain) throws ServletException, IOException {
        Map<String, String> originalContext = MDC.getCopyOfContextMap();

        try {
            Map<String, String> context = resolveContext(originalContext);
            contributors.forEach(contributor -> {
                try {
                    contributor.contribute(context);
                } catch (Exception e) {
                    log.warn("Failed to contribute to ECS logging context using {}", contributor.getClass().getSimpleName(), e);
                }
            });

            MDC.clear();
            if (!context.isEmpty()) {
                MDC.setContextMap(context);
            }

            filterChain.doFilter(servletRequest, servletResponse);
        } finally {
            MDC.clear();
            if (originalContext != null && !originalContext.isEmpty()) {
                MDC.setContextMap(originalContext);
            }
        }
    }

    private static Map<String, String> resolveContext(@Nullable Map<String, String> originalContext) {
        if (originalContext == null) {
            return new HashMap<>();
        }

        return new HashMap<>(originalContext);
    }

}
