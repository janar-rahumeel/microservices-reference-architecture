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
package ee.geckosolutions.mra.token.adapter.in.web;

import ee.geckosolutions.mra.token.adapter.out.api.dto.OAuth2ErrorV1;
import ee.geckosolutions.mra.token.application.exception.InvalidRefreshTokenException;

import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(annotations = OAuth2ErrorResponseV1Api.class)
public class OAuth2ErrorResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(InvalidRefreshTokenException.class)
    public @Nullable ResponseEntity<Object> handleInvalidRefreshTokenException(
            InvalidRefreshTokenException invalidRefreshTokenException,
            WebRequest webRequest) {
        OAuth2ErrorV1 oAuth2ErrorV1Body = OAuth2ErrorV1.builder()
                .error(OAuth2ErrorCodes.INVALID_GRANT)
                .errorDescription("Refresh token is invalid")
                .build();
        return handleExceptionInternal(
                invalidRefreshTokenException,
                oAuth2ErrorV1Body,
                HttpHeaders.EMPTY,
                HttpStatus.BAD_REQUEST,
                webRequest);
    }

    @ExceptionHandler(Exception.class)
    public @Nullable ResponseEntity<Object> handleGeneralException(Exception exception, WebRequest webRequest) {
        OAuth2ErrorV1 oAuth2ErrorV1Body = OAuth2ErrorV1.builder()
                .error(OAuth2ErrorCodes.SERVER_ERROR)
                .errorDescription("An unexpected error occurred")
                .build();
        return handleExceptionInternal(
                exception,
                oAuth2ErrorV1Body,
                HttpHeaders.EMPTY,
                HttpStatus.INTERNAL_SERVER_ERROR,
                webRequest);
    }

    @Override
    protected @Nullable ResponseEntity<Object> handleExceptionInternal(
            Exception exception,
            @Nullable Object body,
            HttpHeaders httpHeaders,
            HttpStatusCode httpStatusCode,
            WebRequest webRequest) {
        if (!(body instanceof OAuth2ErrorV1)) {
            String errorCode = resolveErrorCode(httpStatusCode);
            body = OAuth2ErrorV1.builder().error(errorCode).errorDescription(exception.getMessage()).build();
        }

        OAuth2ErrorV1 oAuth2ErrorV1 = (OAuth2ErrorV1) body;
        if (httpStatusCode.is5xxServerError()) {
            log.error("[{}] Critical error has occurred", oAuth2ErrorV1.getError(), exception);
        } else {
            log.warn("[{}] Non-critical error has occurred", oAuth2ErrorV1.getError(), exception);
        }

        return super.handleExceptionInternal(exception, body, httpHeaders, httpStatusCode, webRequest);
    }

    private static String resolveErrorCode(HttpStatusCode httpStatusCode) {
        if (HttpStatus.BAD_REQUEST.equals(httpStatusCode)) {
            return OAuth2ErrorCodes.INVALID_REQUEST;
        }

        return OAuth2ErrorCodes.SERVER_ERROR;
    }

}
