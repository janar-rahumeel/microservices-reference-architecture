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
package ee.geckosolutions.mra.common.contract.customer.web.dto;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
public class LegalEntityCustomerV2 extends AbstractCustomerV2 {

    private final String name;
    private final String registrationCode;

    @Builder
    @JsonCreator
    public LegalEntityCustomerV2(
            @JsonProperty(value = "id") UUID id,
            @JsonProperty(value = "type") CustomerTypeV2 type,
            @JsonProperty(value = "name") String name,
            @JsonProperty(value = "registrationCode") String registrationCode) {
        super(id, type);
        this.name = name;
        this.registrationCode = registrationCode;
    }

}
