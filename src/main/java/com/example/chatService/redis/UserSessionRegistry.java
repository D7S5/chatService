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

    private static final String PREFIX = "ws:session:";
    private static final Duration TTL = Duration.ofHours(6);

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public void save(String sessionId, String userId, String roomId) {
        UserRoomSession value = new UserRoomSession(userId, roomId);

        try {
            String json = objectMapper.writeValueAsString(value);
            redisTemplate.opsForValue().set(key(sessionId), json, TTL);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("웹소켓 세션 저장 실패", e);
        }
    }

    public UserRoomSession get(String sessionId) {
        String json = redisTemplate.opsForValue().get(key(sessionId));
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
        String redisKey = key(sessionId);
        String json = redisTemplate.opsForValue().get(redisKey);

        if (json == null) {
            return null;
        }

        redisTemplate.delete(redisKey);

        try {
            return objectMapper.readValue(json, UserRoomSession.class);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("웹소켓 세션 삭제 실패", e);
        }
    }

    private String key(String sessionId) {
        return PREFIX + sessionId;
    }
}