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
package ee.geckosolutions.mra.common.platform.observation.ecs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import javax.crypto.SecretKey;

import ee.geckosolutions.mra.common.test.TestJwtUtil;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.amqp.autoconfigure.RabbitAutoConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.bootstrap.DefaultBootstrapContext;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.boot.context.logging.LoggingApplicationListener;
import org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.boot.logging.LoggingSystem;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.oidc.StandardClaimNames;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@ExtendWith(OutputCaptureExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
class EcsLoggingContextSecurityIntegrationTest {

    private static final String TEST_LOGGING_PROPERTIES = "testLoggingProperties";
    private static final String SUBJECT = "04f47c28-9097-455e-806c-1ebc6a6295ed";
    private static final String USER_ID_JSON = "\"id\":\"" + SUBJECT + "\"";
    private static final String USER_NAME_JSON = "\"name\":\"test\"";
    private static final String USER_ROLES_JSON = "\"roles\":[\"USER\"]";

    @Autowired
    private ConfigurableEnvironment configurableEnvironment;

    @Autowired
    private TestRestTemplate testRestTemplate;

    @BeforeEach
    void beforeEach() {
        configurableEnvironment.getPropertySources()
                .addFirst(
                        new MapPropertySource(
                                TEST_LOGGING_PROPERTIES,
                                Map.of(
                                        "logging.structured.format.console",
                                        "ee.geckosolutions.mra.common.platform.observation.ecs.EcsStructuredLogFormatter",
                                        "logging.level.org.zalando.logbook",
                                        "TRACE")));
        resetLoggingSystem(configurableEnvironment);
    }

    @AfterEach
    void afterEach() {
        configurableEnvironment.getPropertySources().remove(TEST_LOGGING_PROPERTIES);
        resetLoggingSystem(configurableEnvironment);
    }

    private static void resetLoggingSystem(ConfigurableEnvironment environment) {
        LoggingSystem loggingSystem = LoggingSystem.get(Thread.currentThread().getContextClassLoader());
        loggingSystem.cleanUp();
        loggingSystem.beforeInitialize();

        LoggingApplicationListener listener = new LoggingApplicationListener();
        listener.onApplicationEvent(
                new ApplicationEnvironmentPreparedEvent(
                        new DefaultBootstrapContext(),
                        new SpringApplication(TestApplication.class),
                        new String[0],
                        environment));
    }

    @Test
    void shouldLogAuthenticatedUserOnRequestAsyncAndLogbookLogs(CapturedOutput capturedOutput) {
        // given
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setBearerAuth(TestJwtUtil.getToken());
        HttpEntity<String> httpEntity = new HttpEntity<>(httpHeaders);

        // when
        ResponseEntity<String> syncResponse = testRestTemplate
                .exchange("/api/v1/sync", HttpMethod.GET, httpEntity, String.class);
        ResponseEntity<String> asyncResponse = testRestTemplate
                .exchange("/api/v1/async", HttpMethod.GET, httpEntity, String.class);

        // then
        assertThat(syncResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(asyncResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        await().untilAsserted(() -> {
            assertThat(logLinesContaining(capturedOutput, "controller sync")).isNotEmpty()
                    .allSatisfy(line -> assertThat(line).contains(USER_ID_JSON, USER_NAME_JSON, USER_ROLES_JSON));
            assertThat(logLinesContaining(capturedOutput, "controller async")).isNotEmpty()
                    .allSatisfy(line -> assertThat(line).contains(USER_ID_JSON, USER_NAME_JSON, USER_ROLES_JSON));
            assertThat(logLinesContaining(capturedOutput, "Request to http://localhost")).hasSize(2)
                    .allSatisfy(line -> assertThat(line).contains(USER_ID_JSON, USER_NAME_JSON, USER_ROLES_JSON));
            assertThat(logLinesContaining(capturedOutput, "Response status code: 200")).hasSize(2)
                    .allSatisfy(line -> assertThat(line).contains(USER_ID_JSON, USER_NAME_JSON, USER_ROLES_JSON));
        });
    }

    private static List<String> logLinesContaining(CapturedOutput capturedOutput, String text) {
        return capturedOutput.getAll().lines().filter(line -> line.contains(text)).toList();
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration(
            exclude = { DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class, RabbitAutoConfiguration.class })
    static class TestApplication {

        @Bean
        JwtDecoder jwtDecoder() {
            SecretKey secretKey = TestJwtUtil.resolveJwtSecretKey();
            return NimbusJwtDecoder.withSecretKey(secretKey).macAlgorithm(MacAlgorithm.HS256).build();
        }

        @Bean
        JwtAuthenticationConverter jwtAuthenticationConverter() {
            JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
            jwtAuthenticationConverter.setJwtPrincipalConverter(
                    jwt -> new DefaultOAuth2User(
                            Set.of(new SimpleGrantedAuthority("ROLE_USER")),
                            jwt.getClaims(),
                            StandardClaimNames.PREFERRED_USERNAME));
            return jwtAuthenticationConverter;
        }

        @Bean
        SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) {
            return httpSecurity.csrf(AbstractHttpConfigurer::disable)
                    .authorizeHttpRequests(registry -> registry.anyRequest().authenticated())
                    .oauth2ResourceServer(configurer -> configurer.jwt(Customizer.withDefaults()))
                    .build();
        }

        @Bean
        TestController testController() {
            return new TestController();
        }

    }

    @Slf4j
    @RestController
    static class TestController {

        @GetMapping("/api/v1/sync")
        public String sync() {
            log.info("controller sync");
            return "ok";
        }

        @GetMapping("/api/v1/async")
        public Callable<String> async() {
            return () -> {
                log.info("controller async");
                return "ok";
            };
        }

    }

}
