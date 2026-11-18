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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.MDC;
import org.springframework.boot.json.JsonWriter;
import org.springframework.boot.logging.structured.StructuredLoggingJsonMembersCustomizer;

public class EcsJsonMembersCustomizer implements StructuredLoggingJsonMembersCustomizer<Object> {

    static final String ECS_PREFIX = "ecs:";
    static final String MULTI_VALUE_SEPARATOR = ",";

    private static final Set<String> MULTI_VALUED_FIELDS = Set.of("user.roles");

    @Override
    public void customize(JsonWriter.Members<Object> members) {
        members.add().usingPairs((ignored, pairs) -> {
            Map<String, Object> fields = new LinkedHashMap<>();
            Map<String, String> mdc = MDC.getCopyOfContextMap();

            if (mdc == null) {
                return;
            }

            mdc.forEach((key, value) -> {
                if (value == null) {
                    return;
                }

                String field = switch (key) {
                case "traceId" -> "trace.id";
                case "spanId" -> "span.id";
                default -> key.startsWith(ECS_PREFIX) ? key.substring(ECS_PREFIX.length()) : null;
                };

                if (field != null) {
                    Object fieldValue = MULTI_VALUED_FIELDS.contains(field)
                            ? List.of(value.split(MULTI_VALUE_SEPARATOR))
                            : value;
                    putNested(fields, field, fieldValue);
                }
            });

            fields.forEach(pairs);
        });
    }

    @SuppressWarnings("unchecked")
    private static void putNested(Map<String, Object> target, String path, Object value) {
        String[] segments = path.split("\\.");
        Map<String, Object> current = target;

        for (int i = 0; i < segments.length - 1; i++) {
            current = (Map<String, Object>) current.computeIfAbsent(segments[i], ignored -> new LinkedHashMap<>());
        }

        current.put(segments[segments.length - 1], value);
    }

}
