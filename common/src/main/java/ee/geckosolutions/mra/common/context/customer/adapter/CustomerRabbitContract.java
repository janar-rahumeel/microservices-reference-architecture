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
package ee.geckosolutions.mra.common.context.customer.adapter;

public final class CustomerRabbitContract {

    public static final String CUSTOMER_EXCHANGE_NAME = "mra.customer";

    private CustomerRabbitContract() {
    }

    public static final class RoutingKeys {

        private RoutingKeys() {
        }

        public static final String CUSTOMER_CREATED_EVENT = "event.customer.created";

    }

}
