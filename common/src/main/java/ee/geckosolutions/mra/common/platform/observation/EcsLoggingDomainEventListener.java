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

import java.util.List;
import java.util.Locale;

import ee.geckosolutions.mra.common.domain.DomainEvent;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;

@Slf4j
public class EcsLoggingDomainEventListener {

    @EventListener
    public void on(DomainEvent<?> domainEvent) {
        String eventType = resolveEventType(domainEvent);
        String eventAction = resolveEventAction(domainEvent);

        log.atInfo()
                .addKeyValue("event.kind", "event")
                .addKeyValue("event.type", List.of(eventType))
                .addKeyValue("event.action", eventAction)
                .log("Domain event {} for entity {}", eventAction.toUpperCase(Locale.ROOT), domainEvent.id());
    }

    private static String resolveEventType(DomainEvent<?> domainEvent) {
        return switch (domainEvent.type()) {
        case CREATE -> "creation";
        case UPDATE -> "change";
        case DELETE -> "deletion";
        };
    }

    private static String resolveEventAction(DomainEvent<?> domainEvent) {
        return domainEvent.getClass()
                .getSimpleName()
                .replaceFirst("Event$", "")
                .replaceAll("([a-z0-9])([A-Z])", "$1_$2")
                .toLowerCase(Locale.ROOT);
    }

}
