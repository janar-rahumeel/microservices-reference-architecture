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
package ee.geckosolutions.mra.common.contract.access.web.dto;

import java.util.Set;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Getter
@Builder
@Jacksonized
public class NewUserV1Request {

    @Size(min = 3, max = 255)
    @Schema(
            requiredMode = Schema.RequiredMode.REQUIRED,
            description = "Username used to identify the user (case-insensitive, stored in lower case)",
            example = "chuck.norris")
    @NotBlank
    private final String username;

    @Size(max = 255)
    @Schema(
            requiredMode = Schema.RequiredMode.REQUIRED,
            description = "First name, held by the identity provider only",
            example = "Chuck")
    @NotBlank
    private final String firstName;

    @Size(max = 255)
    @Schema(
            requiredMode = Schema.RequiredMode.REQUIRED,
            description = "Last name, held by the identity provider only",
            example = "Norris")
    @NotBlank
    private final String lastName;

    @Size(max = 255)
    @Email
    @Schema(
            requiredMode = Schema.RequiredMode.REQUIRED,
            description = "E-mail address, held by the identity provider only",
            example = "jane.doe@example.com")
    @NotBlank
    private final String email;

    @Schema(
            requiredMode = Schema.RequiredMode.REQUIRED,
            description = "Names of the roles to assign to the user",
            example = "[\"USER\"]")
    @NotEmpty
    private final Set<@NotBlank String> roles;

}
