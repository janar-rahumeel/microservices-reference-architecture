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
package ee.geckosolutions.mra.common.contract.web.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public abstract class AbstractPageV1Response<T> {

    @Schema(description = "Elements on the current page")
    private final List<T> elements;

    @Schema(description = "Current zero-based page index", example = "0")
    private final int number;

    @Schema(description = "Number of elements requested per page", example = "20")
    private final int size;

    @Schema(description = "Number of elements on the current page", example = "20")
    private final int numberOfElements;

    @Schema(description = "Total number of available elements", example = "125")
    private final long totalElements;

    @Schema(description = "Total number of pages", example = "7")
    private final int totalPages;

    @Schema(description = "Whether this is the first page", example = "true")
    private final boolean first;

    @Schema(description = "Whether this is the last page", example = "false")
    private final boolean last;

    @Schema(description = "Whether the page contains any elements", example = "true")
    private final boolean empty;

}
