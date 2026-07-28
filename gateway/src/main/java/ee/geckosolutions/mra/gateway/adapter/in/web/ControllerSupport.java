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

import java.util.function.Supplier;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ControllerSupport {

    public static ResponseEntity<byte[]> executeHttpRequest(Supplier<ResponseEntity<byte[]>> clientRequestSupplier) {
        ResponseEntity<byte[]> responseEntity = clientRequestSupplier.get();

        ResponseEntity.BodyBuilder bodyBuilder = ResponseEntity.status(responseEntity.getStatusCode());

        MediaType contentType = responseEntity.getHeaders().getContentType();
        if (contentType != null) {
            bodyBuilder.contentType(contentType);
        }

        return bodyBuilder.body(responseEntity.getBody());
    }

}
