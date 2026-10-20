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
package ee.geckosolutions.mra.core.context.access.application.exception;

import java.util.Optional;

import ee.geckosolutions.mra.common.platform.exception.AbstractApplicationException;
import ee.geckosolutions.mra.common.platform.exception.ApplicationExceptionType;

public class IdentityProviderException extends AbstractApplicationException {

    private static final String ERROR_CODE = "user.technical.identity-provider-failure";

    private final String details;

    public IdentityProviderException(String details) {
        super(ApplicationExceptionType.DEPENDENCY_FAILURE, ERROR_CODE, "Identity provider error");
        this.details = details;
    }

    public Optional<String> getDetails() {
        return Optional.of(details);
    }

}
