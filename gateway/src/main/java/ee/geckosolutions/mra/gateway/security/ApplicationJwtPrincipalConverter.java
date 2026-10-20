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
package ee.geckosolutions.mra.gateway.security;

import java.util.Objects;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.core.oidc.StandardClaimNames;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ApplicationJwtPrincipalConverter implements Converter<Jwt, OAuth2AuthenticatedPrincipal> {

    private final AuthenticationService authenticationService;

    @Override
    public OAuth2AuthenticatedPrincipal convert(Jwt jwt) {
        UUID id = UUID.fromString(Objects.requireNonNull(jwt.getSubject(), "JWT claim 'sub' is missing"));
        User user = authenticationService.getUserById(id);
        return new DefaultOAuth2User(user.getAuthorities(), jwt.getClaims(), StandardClaimNames.PREFERRED_USERNAME);
    }

}
