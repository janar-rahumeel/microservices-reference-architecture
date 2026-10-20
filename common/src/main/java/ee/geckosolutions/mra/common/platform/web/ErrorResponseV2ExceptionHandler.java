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
package ee.geckosolutions.mra.common.platform.web;

import java.util.List;
import java.util.UUID;

import ee.geckosolutions.mra.common.domain.exception.AbstractDomainException;
import ee.geckosolutions.mra.common.domain.exception.DomainExceptionType;
import ee.geckosolutions.mra.common.domain.validation.InvariantViolationException;
import ee.geckosolutions.mra.common.platform.exception.AbstractApplicationException;
import ee.geckosolutions.mra.common.platform.exception.ApplicationExceptionType;
import ee.geckosolutions.mra.common.platform.exception.ErrorDescriptor;
import ee.geckosolutions.mra.common.platform.web.dto.ErrorResponseV2;
import ee.geckosolutions.mra.common.platform.web.dto.FieldErrorV2;

import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.method.MethodValidationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(annotations = ErrorResponseV2Api.class)
public class ErrorResponseV2ExceptionHandler extends ResponseEntityExceptionHandler {

    private static final String GENERAL_ERROR_CODE = "general.technical.error";
    private static final String FIELD_VALIDATION_ERROR_CODE = "general.technical.field-validation";

    @ExceptionHandler(AbstractApplicationException.class)
    public @Nullable ResponseEntity<Object> handleApplicationException(
            AbstractApplicationException applicationException,
            WebRequest webRequest) {
        ErrorResponseV2 errorResponseV2Body = new ErrorResponseV2(
                UUID.randomUUID(),
                applicationException.getErrorCode(),
                applicationException.getMessage());
        HttpStatus httpStatus = map(applicationException.getType());
        return handleExceptionInternal(applicationException, errorResponseV2Body, HttpHeaders.EMPTY, httpStatus, webRequest);
    }

    private static HttpStatus map(ApplicationExceptionType applicationExceptionType) {
        return switch (applicationExceptionType) {
        case ENTITY_NOT_FOUND -> HttpStatus.NOT_FOUND;
        case DEPENDENCY_FAILURE -> HttpStatus.BAD_GATEWAY;
        };
    }

    @ExceptionHandler(InvariantViolationException.class)
    public @Nullable ResponseEntity<Object> handleInvariantViolationException(
            InvariantViolationException invariantViolationException,
            WebRequest webRequest) {
        List<FieldErrorV2> fieldErrors = invariantViolationException.getViolations()
                .stream()
                .map(violation -> new FieldErrorV2(violation.field(), violation.code(), violation.parameters()))
                .toList();
        ErrorResponseV2 errorResponseV2Body = new ErrorResponseV2(
                UUID.randomUUID(),
                invariantViolationException.getErrorCode(),
                invariantViolationException.getMessage(),
                fieldErrors);
        return handleExceptionInternal(
                invariantViolationException,
                errorResponseV2Body,
                HttpHeaders.EMPTY,
                map(invariantViolationException.getType()),
                webRequest);
    }

    @ExceptionHandler(AbstractDomainException.class)
    public @Nullable ResponseEntity<Object> handleDomainException(
            AbstractDomainException domainException,
            WebRequest webRequest) {
        HttpStatus httpStatus = map(domainException.getType());
        ErrorResponseV2 errorResponseV2Body = new ErrorResponseV2(
                UUID.randomUUID(),
                domainException.getErrorCode(),
                domainException.getMessage());
        return handleExceptionInternal(domainException, errorResponseV2Body, HttpHeaders.EMPTY, httpStatus, webRequest);
    }

    private static HttpStatus map(DomainExceptionType domainExceptionType) {
        return switch (domainExceptionType) {
        case INVARIANT_VIOLATION -> HttpStatus.UNPROCESSABLE_CONTENT;
        case DUPLICATE_ENTITY -> HttpStatus.CONFLICT;
        };
    }

    @ExceptionHandler(ResourceAccessException.class)
    public @Nullable ResponseEntity<Object> handleResourceAccessException(
            ResourceAccessException resourceAccessException,
            WebRequest webRequest) {
        ErrorResponseV2 errorResponseV2Body = new ErrorResponseV2(
                UUID.randomUUID(),
                "general.technical.service-unavailable",
                "Downstream service is unavailable");
        return handleExceptionInternal(
                resourceAccessException,
                errorResponseV2Body,
                HttpHeaders.EMPTY,
                HttpStatus.SERVICE_UNAVAILABLE,
                webRequest);
    }

    @ExceptionHandler(Exception.class)
    public @Nullable ResponseEntity<Object> handleGeneralException(Exception exception, WebRequest webRequest) {
        ErrorResponseV2 errorResponseV2Body = new ErrorResponseV2(
                UUID.randomUUID(),
                GENERAL_ERROR_CODE,
                "An unexpected error occurred");
        return handleExceptionInternal(
                exception,
                errorResponseV2Body,
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
        if (!(body instanceof ErrorResponseV2)) {
            String errorCode = resolveErrorCode(exception, httpStatusCode);
            body = new ErrorResponseV2(UUID.randomUUID(), errorCode, exception.getMessage());
        }

        log(((ErrorResponseV2) body).id(), httpStatusCode, exception);
        return super.handleExceptionInternal(exception, body, httpHeaders, httpStatusCode, webRequest);
    }

    private static String resolveErrorCode(Exception exception, HttpStatusCode httpStatusCode) {
        if (exception instanceof MethodArgumentNotValidException || exception instanceof HandlerMethodValidationException
                || exception instanceof MethodValidationException) {
            return FIELD_VALIDATION_ERROR_CODE;
        }
        return resolveErrorCode(httpStatusCode);
    }

    private static String resolveErrorCode(HttpStatusCode httpStatusCode) {
        return switch (httpStatusCode.value()) {
        case 400 -> FIELD_VALIDATION_ERROR_CODE;
        case 404 -> "general.technical.entity-not-found";
        case 409 -> "general.business.duplicate-entity";
        case 422 -> "general.business.rule-violation";
        case 502 -> "general.technical.dependency-failure";
        default -> GENERAL_ERROR_CODE;
        };
    }

    private static void log(UUID errorId, HttpStatusCode httpStatusCode, Exception exception) {
        String message = "[UUID: %s] Request failed with HTTP status %s".formatted(errorId, httpStatusCode);

        if (exception instanceof ErrorDescriptor errorDescriptor) {
            message += ": %s - %s"
                    .formatted(errorDescriptor.getErrorCode(), errorDescriptor.getDetails().orElseGet(exception::getMessage));
        }

        if (httpStatusCode.is5xxServerError()) {
            log.error(message, exception);
        } else if (exception instanceof AbstractDomainException) {
            log.info(message);
        }

        log.warn(message, exception);
    }

}
