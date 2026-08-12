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
package ee.geckosolutions.mra.common.platform.observation;

import java.util.Map;
import java.util.function.Function;

import ch.qos.logback.classic.pattern.ThrowableProxyConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.ThrowableProxy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.slf4j.event.KeyValuePair;
import org.springframework.boot.json.JsonWriter;
import org.springframework.boot.logging.StackTracePrinter;
import org.springframework.boot.logging.structured.ContextPairs;
import org.springframework.boot.logging.structured.ElasticCommonSchemaProperties;
import org.springframework.boot.logging.structured.JsonWriterStructuredLogFormatter;
import org.springframework.boot.logging.structured.StructuredLoggingJsonMembersCustomizer;
import org.springframework.core.env.Environment;
import org.springframework.util.Assert;

/**
 * Custom ECS structured log formatter with an MDC allow-list
 *
 * <p>
 * Only traceId and spanId are mapped from MDC
 * </p>
 *
 * @see org.springframework.boot.logging.logback.ElasticCommonSchemaStructuredLogFormatter
 */
@Slf4j
public class EcsStructuredLogFormatter extends JsonWriterStructuredLogFormatter<ILoggingEvent> {

    private static final JsonWriter.PairExtractor<KeyValuePair> PAIR_EXTRACTOR = JsonWriter.PairExtractor
            .of((pair) -> pair.key, (pair) -> pair.value);

    EcsStructuredLogFormatter(
            Environment environment,
            @Nullable StackTracePrinter stackTracePrinter,
            ContextPairs contextPairs,
            ThrowableProxyConverter throwableProxyConverter,
            StructuredLoggingJsonMembersCustomizer.Builder<?> customizerBuilder) {
        super((members) -> jsonMembers(environment, stackTracePrinter, contextPairs, throwableProxyConverter, members),
              customizerBuilder.nested().build());
    }

    private static void jsonMembers(
            Environment environment,
            @Nullable StackTracePrinter stackTracePrinter,
            ContextPairs contextPairs,
            ThrowableProxyConverter throwableProxyConverter,
            JsonWriter.Members<ILoggingEvent> members) {
        Extractor extractor = new Extractor(stackTracePrinter, throwableProxyConverter);
        members.add("@timestamp", ILoggingEvent::getInstant);
        members.add("log").usingMembers((log) -> {
            log.add("level", ILoggingEvent::getLevel);
            log.add("logger", ILoggingEvent::getLoggerName);
        });
        members.add("process").usingMembers((process) -> {
            process.add("pid", environment.getProperty("spring.application.pid", Long.class)).whenNotNull();
            process.add("thread").usingMembers((thread) -> thread.add("name", ILoggingEvent::getThreadName));
        });
        ElasticCommonSchemaProperties.get(environment).jsonMembers(members);
        members.add("message", ILoggingEvent::getFormattedMessage);
        members.add().usingPairs(contextPairs.nested((pairs) -> {
            pairs.add((event, consumer) -> {
                Map<String, String> mdcPropertyMap = event.getMDCPropertyMap();

                String traceId = mdcPropertyMap.get("traceId");
                if (traceId != null) {
                    consumer.accept("trace.id", traceId);
                }

                String spanId = mdcPropertyMap.get("spanId");
                if (spanId != null) {
                    consumer.accept("span.id", spanId);
                }
            });
            pairs.add(ILoggingEvent::getKeyValuePairs, PAIR_EXTRACTOR);
        }));
        Function<@Nullable ILoggingEvent, @Nullable Object> getThrowableProxy = (
                event) -> (event != null) ? event.getThrowableProxy() : null;
        members.add().whenNotNull(getThrowableProxy).usingMembers((throwableMembers) -> {
            throwableMembers.add("error").usingMembers((error) -> {
                error.add("type", ILoggingEvent::getThrowableProxy).as(IThrowableProxy::getClassName);
                error.add("message", ILoggingEvent::getThrowableProxy).as(IThrowableProxy::getMessage);
                error.add("stack_trace", extractor::stackTrace);
            });
        });
        members.add("ecs").usingMembers((ecs) -> ecs.add("version", "8.11"));
    }

    /**
     * @see org.springframework.boot.logging.logback.Extractor
     */
    @RequiredArgsConstructor
    static class Extractor {

        private final @Nullable StackTracePrinter stackTracePrinter;
        private final ThrowableProxyConverter throwableProxyConverter;

        String stackTrace(ILoggingEvent event) {
            if (this.stackTracePrinter != null) {
                IThrowableProxy throwableProxy = event.getThrowableProxy();
                Assert.state(
                        throwableProxy instanceof ThrowableProxy,
                        "Instance must be a ThrowableProxy in order to print exception");
                Throwable throwable = ((ThrowableProxy) throwableProxy).getThrowable();
                return this.stackTracePrinter.printStackTraceToString(throwable);
            }
            return this.throwableProxyConverter.convert(event);
        }

    }

}
