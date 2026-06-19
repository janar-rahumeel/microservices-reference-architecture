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
package ee.geckosolutions.mra.gateway.adapter.in.web;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import ee.geckosolutions.mra.gateway.config.ApplicationProperties;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ControllerSupport {

    private static final DateTimeFormatter HTTP_DATE_FORMAT = DateTimeFormatter.RFC_1123_DATE_TIME.withZone(ZoneOffset.UTC);

    private final ApplicationProperties applicationProperties;

    public <T> ResponseEntity<T> forwardWithDeprecationHeaders(ResponseEntity<T> responseEntity, Instant deprecatedSince) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Deprecation", "@" + deprecatedSince.getEpochSecond());
        httpHeaders.add(
                "Sunset",
                HTTP_DATE_FORMAT.format(
                        deprecatedSince.atZone(ZoneOffset.UTC)
                                .plus(applicationProperties.getApiDeprecationSunsetPeriod())
                                .toInstant()));
        httpHeaders.setContentType(responseEntity.getHeaders().getContentType());

        return new ResponseEntity<>(responseEntity.getBody(), httpHeaders, responseEntity.getStatusCode());
    }

}
