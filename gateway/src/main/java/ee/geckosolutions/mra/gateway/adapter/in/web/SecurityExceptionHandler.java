/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright (C) 2026-present Gecko Solutions OU
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
package ee.geckosolutions.mra.gateway.adapter.in.web;

import java.io.IOException;
import java.util.UUID;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import ee.geckosolutions.mra.common.platform.validation.ErrorCode;
import ee.geckosolutions.mra.common.platform.web.dto.ErrorResponseV2;
import ee.geckosolutions.mra.gateway.config.ApplicationProperties;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.oauth2.server.resource.web.access.BearerTokenAccessDeniedHandler;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Component
@RequiredArgsConstructor
public class SecurityExceptionHandler implements AuthenticationEntryPoint, AccessDeniedHandler {

    private final ApplicationProperties applicationProperties;
    private final ObjectMapper objectMapper;
    private final BearerTokenAuthenticationEntryPoint bearerTokenAuthenticationEntryPoint = new BearerTokenAuthenticationEntryPoint();
    private final BearerTokenAccessDeniedHandler bearerTokenAccessDeniedHandler = new BearerTokenAccessDeniedHandler();

    @Override
    public void commence(
            HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse,
            AuthenticationException authenticationException) throws IOException {
        bearerTokenAuthenticationEntryPoint.commence(httpServletRequest, httpServletResponse, authenticationException);

        if (shouldUseDefaultErrorResponse(httpServletRequest)) {
            return;
        }

        if (!httpServletResponse.isCommitted()) {
            UUID errorId = UUID.randomUUID();
            log.warn("[UUID: {}] Non-critical error has occurred", errorId, authenticationException);
            writeErrorResponseV2(
                    httpServletResponse,
                    errorId,
                    () -> "general.technical.unauthorized",
                    "Authentication is required");
        }
    }

    @Override
    public void handle(
            HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse,
            AccessDeniedException accessDeniedException) throws IOException {
        bearerTokenAccessDeniedHandler.handle(httpServletRequest, httpServletResponse, accessDeniedException);

        if (shouldUseDefaultErrorResponse(httpServletRequest)) {
            return;
        }

        if (!httpServletResponse.isCommitted()) {
            UUID errorId = UUID.randomUUID();
            log.warn("[UUID: {}] Non-critical error has occurred", errorId, accessDeniedException);
            writeErrorResponseV2(httpServletResponse, errorId, () -> "general.technical.forbidden", "Access is denied");
        }
    }

    private static boolean shouldUseDefaultErrorResponse(HttpServletRequest httpServletRequest) {
        return httpServletRequest.getRequestURI().startsWith("/api/v1/");
    }

    private void writeErrorResponseV2(
            HttpServletResponse httpServletResponse,
            UUID errorId,
            ErrorCode errorCode,
            String message) throws IOException {
        httpServletResponse.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(httpServletResponse.getOutputStream(), new ErrorResponseV2(errorId, errorCode, message));
    }

    @PostConstruct
    void init() {
        bearerTokenAuthenticationEntryPoint.setRealmName(applicationProperties.getSecurity().getRealmName());
        bearerTokenAccessDeniedHandler.setRealmName(applicationProperties.getSecurity().getRealmName());
    }

}
