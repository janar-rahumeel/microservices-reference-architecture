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
package ee.geckosolutions.mra.token.adapter.out.repository;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Optional;

import ee.geckosolutions.mra.token.application.port.RefreshTokenRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisRefreshTokenRepository implements RefreshTokenRepository {

    private static final String KEY_PREFIX = "oauth2:refresh-token:";

    private final StringRedisTemplate redisTemplate;

    @Override
    public void save(String id, String token, Duration ttl) {
        String key = KEY_PREFIX + id;
        log.debug("Saving token {}={}", key, DigestUtils.md5DigestAsHex(token.getBytes(StandardCharsets.UTF_8)));
        redisTemplate.opsForValue().set(key, token, ttl);
    }

    @Override
    public Optional<String> get(String id) {
        String key = KEY_PREFIX + id;
        log.debug("Accessing token {}", key);
        return Optional.ofNullable(redisTemplate.opsForValue().get(key));
    }

    @Override
    public void delete(String id) {
        String key = KEY_PREFIX + id;
        log.debug("Deleting token {}", key);
        redisTemplate.delete(key);
    }

}
