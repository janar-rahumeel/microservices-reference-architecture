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
package ee.geckosolutions.mra.common.platform.http;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class HttpClientUtil {

    public static RestClient.Builder customize(RestClient.Builder builder, HttpServiceProperties httpServiceProperties) {
        return builder.baseUrl(httpServiceProperties.getBaseUrl());
    }

    public static <T> T create(RestClient.Builder builder, Class<T> httpServiceType) {
        HttpServiceProxyFactory httpServiceProxyFactory = HttpServiceProxyFactory
                .builderFor(RestClientAdapter.create(builder.build()))
                .build();
        return httpServiceProxyFactory.createClient(httpServiceType);
    }

}
