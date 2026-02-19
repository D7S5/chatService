package com.example.chatService.redis;

import com.example.chatService.dto.WsTokenResponse;
import com.example.chatService.security.CookieUtil;
import com.example.chatService.security.JwtTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WsTokenService {

    private final WsTokenService wsTokenService;
    private final CookieUtil cookieUtil;
    private final JwtTokenProvider jwtTokenProvider;

    private static final String KEY_PREFIX = "ws_token:";
    private static final int TTL_SECONDS = 120;
    private final RedisTemplate<String, Object> redisTemplate;

    public String createTokenForUser(String userId) {
        String token = UUID.randomUUID().toString();
        String key = KEY_PREFIX + token;
        redisTemplate.opsForValue().set(key, userId, Duration.ofSeconds(TTL_SECONDS));
        return token;
    }

    // 조회 (and delete to enforce 1회용)
    public String consumeToken(String token) {
        String key = KEY_PREFIX + token;
        String userId = (String) redisTemplate.opsForValue().get(key);
        if (userId != null) {
            // 1회용: 즉시 삭제
            redisTemplate.delete(key);
        }
        return userId;
    }

    public String peekToken(String token) {
        return (String) redisTemplate.opsForValue().get(KEY_PREFIX + token);
    }
}
