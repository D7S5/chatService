package com.example.chatService.redis;

import com.example.chatService.dto.UserRoomSession;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class UserSessionRegistry {

    private static final String DATA_PREFIX = "ws:session:data:";
    private static final String TTL_PREFIX = "ws:session:ttl:";
    private static final Duration TTL = Duration.ofMinutes(2);

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public void save(String sessionId, String userId, String roomId) {
        UserRoomSession value = new UserRoomSession(userId, roomId);

        try {
            String json = objectMapper.writeValueAsString(value);
            redisTemplate.opsForValue().set(dataKey(sessionId), json);
            redisTemplate.opsForValue().set(ttlKey(sessionId), "1", TTL);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("웹소켓 세션 저장 실패", e);
        }
    }

    public UserRoomSession get(String sessionId) {
        String json = redisTemplate.opsForValue().get(dataKey(sessionId));
        if (json == null) {
            return null;
        }

        try {
            return objectMapper.readValue(json, UserRoomSession.class);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("웹소켓 세션 조회 실패", e);
        }
    }

    public UserRoomSession remove(String sessionId) {
        String redisKey = dataKey(sessionId);
        String json = redisTemplate.opsForValue().get(redisKey);

        if (json == null) {
            redisTemplate.delete(ttlKey(sessionId));
            return null;
        }

        redisTemplate.delete(redisKey);
        redisTemplate.delete(ttlKey(sessionId));

        try {
            return objectMapper.readValue(json, UserRoomSession.class);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("웹소켓 세션 삭제 실패", e);
        }
    }

    public void refreshTtl(String sessionId) {
        if (Boolean.TRUE.equals(redisTemplate.hasKey(dataKey(sessionId)))) {
            redisTemplate.opsForValue().set(ttlKey(sessionId), "1", TTL);
        }
    }

    public UserRoomSession removeByTtlKey(String redisKey) {
        if (!redisKey.startsWith(TTL_PREFIX)) {
            return null;
        }

        String sessionId = redisKey.substring(TTL_PREFIX.length());
        return remove(sessionId);
    }

    public boolean isTtlKey(String redisKey) {
        return redisKey.startsWith(TTL_PREFIX);
    }

    private String dataKey(String sessionId) {
        return DATA_PREFIX + sessionId;
    }

    private String ttlKey(String sessionId) {
        return TTL_PREFIX + sessionId;
    }
}
