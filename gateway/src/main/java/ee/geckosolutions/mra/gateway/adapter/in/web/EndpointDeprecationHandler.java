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

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import ee.geckosolutions.mra.gateway.config.ApplicationProperties;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.accept.ApiVersionDeprecationHandler;
import org.springframework.web.servlet.HandlerMapping;

@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class EndpointDeprecationHandler implements ApiVersionDeprecationHandler {

    private final Map<EndpointKey, Map<String, String>> endpointDeprecationHeadersMap;

    public static EndpointDeprecationHandler of(ApplicationProperties applicationProperties) {
        Map<EndpointKey, Map<String, String>> endpointDeprecationHeadersMap = applicationProperties.getApi()
                .getDeprecatedEndpoints()
                .stream()
                .flatMap(
                        endpoint -> endpoint.methods()
                                .stream()
                                .map(
                                        method -> Map.entry(
                                                new EndpointKey(endpoint.pathTemplate(), method),
                                                toDeprecationEndpointHeaders(applicationProperties).apply(endpoint))))
                .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue));
        return new EndpointDeprecationHandler(endpointDeprecationHeadersMap);
    }

    private static Function<ApplicationProperties.DeprecatedEndpoint, Map<String, String>> toDeprecationEndpointHeaders(
            ApplicationProperties applicationProperties) {
        return deprecationEndpoint -> {
            Map<String, String> headers = new HashMap<>();
            ZonedDateTime deprecationDate = deprecationEndpoint.deprecationDate();
            headers.put("Deprecation", "@" + deprecationDate.toInstant().getEpochSecond());
            headers.put(
                    "Sunset",
                    Objects.requireNonNullElseGet(
                            deprecationEndpoint.sunsetDate(),
                            () -> deprecationDate.plus(applicationProperties.getApi().getDeprecationSunsetPeriod()))
                            .format(DateTimeFormatter.RFC_1123_DATE_TIME));

            if (StringUtils.isNotBlank(deprecationEndpoint.successorLink())) {
                headers.put(
                        HttpHeaders.LINK,
                        String.format("<%s>; rel=\"successor-version\"", deprecationEndpoint.successorLink()));
            }

            return Map.copyOf(headers);
        };
    }

    @Override
    public void handleVersion(Comparable<?> ignored, Object handler, HttpServletRequest request, HttpServletResponse response) {
        String pathTemplate = (String) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        if (StringUtils.isNotBlank(pathTemplate)) {
            Map<String, String> endpointDeprecationHeaders = endpointDeprecationHeadersMap
                    .get(new EndpointKey(pathTemplate, HttpMethod.valueOf(request.getMethod())));
            if (endpointDeprecationHeaders != null) {
                endpointDeprecationHeaders.forEach(response::addHeader);
            }
        } else {
            log.warn("No path template for {}", request.getRequestURI());
        }
    }

    private record EndpointKey(String pathTemplate, HttpMethod method) {
    }

}
