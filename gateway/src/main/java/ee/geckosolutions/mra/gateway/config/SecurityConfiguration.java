/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright (C) 2026-present Gecko Solutions OU
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
package ee.geckosolutions.mra.gateway.config;

import java.time.Duration;
import java.util.Set;

import ee.geckosolutions.mra.gateway.adapter.in.web.SecurityExceptionHandler;

import com.nimbusds.jose.jwk.source.JWKSourceBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.boot.http.client.HttpClientSettings;
import org.springframework.boot.restclient.RestTemplateBuilder;
import org.springframework.boot.security.oauth2.server.resource.autoconfigure.servlet.JwkSetUriJwtDecoderBuilderCustomizer;
import org.springframework.boot.ssl.NoSuchSslBundleException;
import org.springframework.boot.ssl.SslBundle;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfiguration {

    private static final Set<String> PERMITTED_REQUEST_URI_PATTERNS = Set
            .of("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**", "/v3/api-docs.yaml", "/v3/api-docs");

    private final SecurityExceptionHandler securityExceptionHandler;

    @Bean
    SecurityFilterChain securityFilterChainOAuth2(HttpSecurity httpSecurity) {
        return httpSecurity.securityMatcher("/**")
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(
                        registry -> registry.requestMatchers(PERMITTED_REQUEST_URI_PATTERNS.toArray(String[]::new))
                                .permitAll()
                                .anyRequest()
                                .authenticated())
                .oauth2ResourceServer(
                        configurer -> configurer.jwt(Customizer.withDefaults())
                                .authenticationEntryPoint(securityExceptionHandler)
                                .accessDeniedHandler(securityExceptionHandler))
                .build();
    }

    @Bean
    JwkSetUriJwtDecoderBuilderCustomizer jwkSetUriJwtDecoderBuilderCustomizer(
            RestTemplateBuilder restTemplateBuilder,
            SslBundles sslBundles) {
        HttpClientSettings httpClientSettings = new HttpClientSettings(
                null,
                Duration.ofMillis(JWKSourceBuilder.DEFAULT_HTTP_CONNECT_TIMEOUT),
                Duration.ofMillis(JWKSourceBuilder.DEFAULT_HTTP_READ_TIMEOUT),
                resolveRootCaBundle(sslBundles));
        RestTemplate restTemplate = restTemplateBuilder.clientSettings(httpClientSettings).build();
        return jwkSetUriJwtDecoderBuilder -> jwkSetUriJwtDecoderBuilder.restOperations(restTemplate);
    }

    private static @Nullable SslBundle resolveRootCaBundle(SslBundles sslBundles) {
        try {
            return sslBundles.getBundle("root-ca");
        } catch (NoSuchSslBundleException e) {
            log.warn(e.getMessage());
            return null;
        }
    }

}
