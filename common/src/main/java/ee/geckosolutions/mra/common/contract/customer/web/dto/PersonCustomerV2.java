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

import java.util.Objects;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
public class PersonCustomerV2 extends AbstractCustomerV2 {

    @NotBlank
    private final String firstName;
    @NotBlank
    private final String lastName;
    @NotBlank
    private final String personalIdentificationCode;

    @Builder
    @JsonCreator
    public PersonCustomerV2(
            @JsonProperty(value = "id", required = true) UUID id,
            @JsonProperty(value = "type", required = true) CustomerTypeV2 type,
            @JsonProperty(value = "firstName", required = true) String firstName,
            @JsonProperty(value = "lastName", required = true) String lastName,
            @JsonProperty(value = "personalIdentificationCode", required = true) String personalIdentificationCode) {
        super(id, type);
        this.firstName = Objects.requireNonNull(firstName, "firstName");
        this.lastName = Objects.requireNonNull(lastName, "lastName");
        this.personalIdentificationCode = Objects.requireNonNull(personalIdentificationCode, "personalIdentificationCode");
    }

}
