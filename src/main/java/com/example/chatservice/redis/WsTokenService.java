package com.example.chatservice.redis;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Service
public class WsTokenService {
    private static final String KEY_PREFIX = "ws_token:";
    private static final long TTL_SECONDS = 120;

    private final RedisTemplate<String, String> redisTemplate;

    public WsTokenService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public String createTokenForUser(String userId) {
        String token = UUID.randomUUID().toString();
        String key = KEY_PREFIX + token;
        redisTemplate.opsForValue().set(key, userId, Duration.ofSeconds(TTL_SECONDS));
        return token;
    }

    // 조회 (and delete to enforce 1회용)
    public String consumeToken(String token) {
        String key = KEY_PREFIX + token;
        String userId = redisTemplate.opsForValue().get(key);
        if (userId != null) {
            // 1회용: 즉시 삭제
            redisTemplate.delete(key);
        }
        return userId;
    }

    public String peekToken(String token) {
        return redisTemplate.opsForValue().get(KEY_PREFIX + token);
    }
}
