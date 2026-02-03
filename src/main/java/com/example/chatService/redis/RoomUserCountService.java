package com.example.chatService.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoomUserCountService {

    private final StringRedisTemplate redisTemplate;

    private static final String PREFIX = "room:";

    public long increment(String roomId) {
        return redisTemplate.opsForValue()
                .increment(PREFIX + roomId + ":count");
    }

    public long decrement(String roomId) {
        Long val = redisTemplate.opsForValue()
                .decrement(PREFIX + roomId + ":count");
        return val != null && val >= 0 ? val : 0;
    }

    public long getCount(Long roomId) {
        String val = redisTemplate.opsForValue()
                .get(PREFIX + roomId + ":count");
        return val == null ? 0  : Long.parseLong(val);
    }
}
