package com.example.chatservice.component;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class ChatRateLimiter {

    private final StringRedisTemplate redis;

    private static final int USER_LIMIT_PER_SEC = 7;
    private static final int ROOM_LIMIT_PER_SEC = 1000;

    private static final int BAN_SECONDS = 30;

    private String rateKey(String userId) {
        return "chat:rate:user:" + userId;
    }

    private String banKey(String userId) {
        return "chat:ban:user" + userId;
    }

    private boolean isBanned(String userId) {
        return Boolean.TRUE.equals(redis.hasKey(banKey(userId)));
    }

    public long getBanTtl(String userId) {
        Long ttl = redis.getExpire(banKey(userId));
        return ttl != null ? ttl : 0;
    }

    public boolean allowOrBan(String userId) {

        if (isBanned(userId)) {
            return false;
        }

        Long count = redis.opsForValue().increment(rateKey(userId));

        if ( count != null && count == 1) {
            redis.expire(rateKey(userId), Duration.ofSeconds(1));
        }

        if (count != null && count > USER_LIMIT_PER_SEC) {
            redis.opsForValue().set(
                    banKey(userId),
                    "1",
                    Duration.ofSeconds(BAN_SECONDS)
            );
            return false;
        }

        return true;
    }
    /**
     * 유저 초당 제한
     * */
    public boolean allowUser(String userId) {

        String key = "chat:rate:user:" + userId;
        Long count = redis.opsForValue().increment(key);

        if (count != null && count == 1) {
            redis.expire(key, Duration.ofSeconds(1));
        }

        return count != null && count <= USER_LIMIT_PER_SEC;
    }

    public boolean allowRoom(String roomId) {
        String key = "chat:rate:room:" + roomId;

        Long count = redis.opsForValue().increment(key);

        if (count != null && count == 1) {
            redis.expire(key, Duration.ofSeconds(1));
        }

        return count != null && count <= ROOM_LIMIT_PER_SEC;
    }
}
