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
import java.util.concurrent.Callable;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.amqp.autoconfigure.RabbitAutoConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@ExtendWith(OutputCaptureExtension.class)
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "logging.structured.format.console=ee.geckosolutions.mra.common.platform.observation.ecs.EcsStructuredLogFormatter",
                "logging.level.org.zalando.logbook=TRACE" })
@AutoConfigureTestRestTemplate
class EcsLoggingContextSecurityIntegrationTest {

    private static final String USER_NAME_JSON = "\"user\":{\"name\":\"test\"}";

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Test
    void shouldLogAuthenticatedUserOnRequestAsyncAndLogbookLogs(CapturedOutput capturedOutput) {
        // when
        ResponseEntity<String> syncResponse = testRestTemplate.withBasicAuth("test", "test")
                .getForEntity("/api/v1/sync", String.class);
        ResponseEntity<String> asyncResponse = testRestTemplate.withBasicAuth("test", "test")
                .getForEntity("/api/v1/async", String.class);

        // then
        assertThat(syncResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(asyncResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        await().untilAsserted(() -> {
            assertThat(logLinesContaining(capturedOutput, "controller sync")).isNotEmpty()
                    .allSatisfy(line -> assertThat(line).contains(USER_NAME_JSON));
            assertThat(logLinesContaining(capturedOutput, "controller async")).isNotEmpty()
                    .allSatisfy(line -> assertThat(line).contains(USER_NAME_JSON));
            assertThat(logLinesContaining(capturedOutput, "Request to http://localhost")).hasSize(2)
                    .allSatisfy(line -> assertThat(line).contains(USER_NAME_JSON));
            assertThat(logLinesContaining(capturedOutput, "Response status code: 200")).hasSize(2)
                    .allSatisfy(line -> assertThat(line).contains(USER_NAME_JSON));
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
        SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) {
            return httpSecurity.csrf(AbstractHttpConfigurer::disable)
                    .authorizeHttpRequests(registry -> registry.anyRequest().authenticated())
                    .httpBasic(Customizer.withDefaults())
                    .build();
        }

        @Bean
        UserDetailsService userDetailsService() {
            return new InMemoryUserDetailsManager(User.withUsername("test").password("{noop}test").roles("USER").build());
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
