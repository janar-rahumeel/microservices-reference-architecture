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

import jakarta.validation.constraints.NotBlank;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
public class NewLegalEntityCustomerV2 extends AbstractNewCustomerV2 {

    @NotBlank
    private final String name;
    @NotBlank
    private final String registrationCode;

    @Builder
    @JsonCreator
    public NewLegalEntityCustomerV2(
            @JsonProperty("name") String name,
            @JsonProperty("registrationCode") String registrationCode) {
        super();
        this.name = name;
        this.registrationCode = registrationCode;
    }

}
