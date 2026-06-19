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
public class PersonCustomerV2 extends AbstractCustomerV2 {

    private final String firstName;
    private final String lastName;
    private final String personalIdentificationCode;

    @Builder
    @JsonCreator
    public PersonCustomerV2(
            @JsonProperty(value = "id") UUID id,
            @JsonProperty(value = "firstName") String firstName,
            @JsonProperty(value = "lastName") String lastName,
            @JsonProperty(value = "personalIdentificationCode") String personalIdentificationCode) {
        super(id);
        this.firstName = firstName;
        this.lastName = lastName;
        this.personalIdentificationCode = personalIdentificationCode;
    }

}
