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
package ee.geckosolutions.mra.token.adapter.out.api;

import ee.geckosolutions.mra.token.adapter.out.api.dto.OAuth2ErrorV1;

import lombok.Getter;
import org.springframework.http.HttpStatusCode;

@Getter
public class KeycloakOAuth2ErrorV1Exception extends RuntimeException {

    private final HttpStatusCode httpStatusCode;
    private final OAuth2ErrorV1 oAuth2ErrorV1;

    public KeycloakOAuth2ErrorV1Exception(HttpStatusCode httpStatusCode, OAuth2ErrorV1 oAuth2ErrorV1) {
        super(oAuth2ErrorV1.getErrorDescription());
        this.httpStatusCode = httpStatusCode;
        this.oAuth2ErrorV1 = oAuth2ErrorV1;
    }

}
