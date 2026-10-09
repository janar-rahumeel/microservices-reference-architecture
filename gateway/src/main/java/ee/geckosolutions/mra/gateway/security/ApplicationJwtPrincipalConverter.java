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

import java.util.ArrayList;
import java.util.Objects;

import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.DefaultOAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ApplicationJwtPrincipalConverter implements Converter<Jwt, OAuth2AuthenticatedPrincipal> {

    private final ApplicationUserDetailsService userDetailsService;

    @Override
    public OAuth2AuthenticatedPrincipal convert(Jwt jwt) {
        String username = Objects
                .requireNonNull(jwt.getClaimAsString("preferred_username"), "JWT claim 'preferred_username' is missing");
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        return new DefaultOAuth2AuthenticatedPrincipal(
                userDetails.getUsername(),
                jwt.getClaims(),
                new ArrayList<>(userDetails.getAuthorities()));
    }

}
