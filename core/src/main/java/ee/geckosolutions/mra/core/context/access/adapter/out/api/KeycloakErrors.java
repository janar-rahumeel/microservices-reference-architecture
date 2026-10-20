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
package ee.geckosolutions.mra.core.context.access.adapter.out.api;

import java.util.Optional;

import ee.geckosolutions.mra.core.context.access.adapter.out.api.dto.KeycloakErrorV1Response;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.web.client.RestClientResponseException;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class KeycloakErrors {

    static String details(String message, RestClientResponseException exception) {
        return message + ": " + exception.getStatusCode() + reason(exception).map(reason -> " - " + reason).orElse("");
    }

    private static Optional<String> reason(RestClientResponseException exception) {
        if (exception.getResponseBodyAsByteArray().length == 0) {
            return Optional.empty();
        }

        try {
            return Optional.ofNullable(exception.getResponseBodyAs(KeycloakErrorV1Response.class))
                    .map(KeycloakErrorV1Response::message);
        } catch (Exception ignored) {
            return Optional.empty();
        }
    }

}
