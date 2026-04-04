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

import java.util.UUID;

import ee.geckosolutions.mra.common.domain.exception.AbstractDomainException;
import ee.geckosolutions.mra.common.domain.exception.DomainExceptionType;
import ee.geckosolutions.mra.common.platform.exception.AbstractApplicationException;
import ee.geckosolutions.mra.common.platform.exception.ApplicationExceptionType;
import ee.geckosolutions.mra.common.platform.validation.ErrorCode;
import ee.geckosolutions.mra.common.platform.web.dto.ErrorResponseV2;

import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
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
@RestControllerAdvice(annotations = ErrorResponseV2Api.class)
public class ErrorResponseV2ExceptionHandler extends ResponseEntityExceptionHandler {

    private static final ErrorCode GENERAL_ERROR_CODE = () -> "general.technical.error";
    private static final ErrorCode FIELD_VALIDATION_ERROR_CODE = () -> "general.technical.field-validation";

    @ExceptionHandler(AbstractApplicationException.class)
    public ResponseEntity<Object> handleApplicationException(
            AbstractApplicationException applicationException,
            WebRequest webRequest) {
        HttpStatus httpStatus = map(applicationException.getType());
        ErrorResponseV2 errorResponseV2Body = new ErrorResponseV2(
                UUID.randomUUID(),
                applicationException.getErrorCode(),
                applicationException.getMessage());
        return handleExceptionInternal(applicationException, errorResponseV2Body, new HttpHeaders(), httpStatus, webRequest);
    }

    private static HttpStatus map(ApplicationExceptionType applicationExceptionType) {
        return switch (applicationExceptionType) {
        case ENTITY_NOT_FOUND -> HttpStatus.NOT_FOUND;
        };
    }

    @ExceptionHandler(AbstractDomainException.class)
    public ResponseEntity<Object> handleDomainException(AbstractDomainException domainException, WebRequest webRequest) {
        HttpStatus httpStatus = map(domainException.getType());
        ErrorResponseV2 errorResponseV2Body = new ErrorResponseV2(
                UUID.randomUUID(),
                domainException::getErrorCode,
                domainException.getMessage());
        return handleExceptionInternal(domainException, errorResponseV2Body, new HttpHeaders(), httpStatus, webRequest);
    }

    private static HttpStatus map(DomainExceptionType domainExceptionType) {
        return switch (domainExceptionType) {
        case FIELD_VALIDATION -> HttpStatus.BAD_REQUEST;
        case DUPLICATE_ENTITY -> HttpStatus.CONFLICT;
        };
    }

    @ExceptionHandler(ResourceAccessException.class)
    public ResponseEntity<Object> handleResourceAccessException(
            ResourceAccessException resourceAccessException,
            WebRequest webRequest) {
        ErrorResponseV2 errorResponseV2Body = new ErrorResponseV2(
                UUID.randomUUID(),
                () -> "general.technical.service-unavailable",
                "Downstream service is unavailable");
        return handleExceptionInternal(
                resourceAccessException,
                errorResponseV2Body,
                new HttpHeaders(),
                HttpStatus.SERVICE_UNAVAILABLE,
                webRequest);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGeneralException(Exception exception, WebRequest webRequest) {
        ErrorResponseV2 errorResponseV2Body = new ErrorResponseV2(
                UUID.randomUUID(),
                GENERAL_ERROR_CODE,
                "An unexpected error occurred");
        return handleExceptionInternal(
                exception,
                errorResponseV2Body,
                new HttpHeaders(),
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
            ErrorCode errorCode = resolveErrorCode(exception, httpStatusCode);
            body = new ErrorResponseV2(UUID.randomUUID(), errorCode, exception.getMessage());
        }

        ErrorResponseV2 errorResponseV2 = (ErrorResponseV2) body;
        if (exception instanceof AbstractDomainException domainException) {
            log.warn("[UUID: {}] Domain exception has been occurred", errorResponseV2.id(), domainException);
        } else {
            log.error("[UUID: {}] Error has been occurred", errorResponseV2.id(), exception);
        }

        return super.handleExceptionInternal(exception, body, httpHeaders, httpStatusCode, webRequest);
    }

    private static ErrorCode resolveErrorCode(Exception exception, HttpStatusCode httpStatusCode) {
        if (exception instanceof MethodArgumentNotValidException || exception instanceof HandlerMethodValidationException
                || exception instanceof MethodValidationException) {
            return FIELD_VALIDATION_ERROR_CODE;
        }
        return resolveErrorCode(httpStatusCode);
    }

    private static ErrorCode resolveErrorCode(HttpStatusCode httpStatusCode) {
        return switch (httpStatusCode.value()) {
        case 400 -> FIELD_VALIDATION_ERROR_CODE;
        case 404 -> () -> "general.technical.entity-not-found";
        case 409 -> () -> "general.business.duplicate-entity";
        case 422 -> () -> "general.business.rule-violation";
        default -> GENERAL_ERROR_CODE;
        };
    }

}
