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
package ee.geckosolutions.mra.common.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public abstract class AggregateRoot {

    private final List<DomainEvent> domainEvents = new ArrayList<>();

    protected void registerEvent(DomainEvent domainEvent) {
        this.domainEvents.add(Objects.requireNonNull(domainEvent, "Domain event must not be null"));
    }

    public List<DomainEvent> drainDomainEvents() {
        List<DomainEvent> copyOfDomainEvents = List.copyOf(domainEvents);
        domainEvents.clear();
        return copyOfDomainEvents;
    }

}
