/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright (C) 2026-present Gecko Solutions OÜ
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ee.geckosolutions.mra.core.context.access.adapter.out.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jspecify.annotations.Nullable;

@JsonIgnoreProperties(ignoreUnknown = true)
public record KeycloakErrorV1Response(@Nullable String error,
        @JsonProperty("error_description") @Nullable String errorDescription, @Nullable String errorMessage) {

    public @Nullable String message() {
        if (isPresent(errorMessage)) {
            return errorMessage;
        }

        return isPresent(errorDescription) ? errorDescription : isPresent(error) ? error : null;
    }

    private static boolean isPresent(@Nullable String value) {
        return value != null && !value.isBlank();
    }

}
