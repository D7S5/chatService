package com.example.chatservice.redis;

import com.example.chatservice.dto.OnlineStatusDto;
import com.example.chatservice.dto.UserEnterDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OnlineStatusServiceV3 {

    private final StringRedisTemplate redis;
    private final SimpMessagingTemplate messaging;

    private static final String ONLINE_USERS = "online:users";
    private static final Duration CONN_TTL = Duration.ofSeconds(40);

    public void connect(UserEnterDto dto, String connId) {
        String userId = dto.getUserId();

        redis.opsForValue().set(
                "conn:" + connId,
                userId,
                CONN_TTL
        );

        redis.opsForSet()
                .add("user:" + userId + ":conns", connId);

        Long count = redis.opsForSet()
                .size("user:" + userId + ":conns");

        System.out.println("size = " + count);
        // 첫 연결 → 온라인
        if (count != null && count == 1) {
            redis.opsForHash()
                    .put(ONLINE_USERS, userId, dto.getUsername());
            broadcast();
        }
    }

    public void heartbeat(String connId) {
        redis.expire("conn:" + connId, CONN_TTL);
    }

    public void disconnectByTTL(String connId) {

        String userId = redis.opsForValue()
                .get("conn:" + connId);

        if (userId == null) return;

        redis.opsForSet()
                .remove("user:" + userId + ":conns", connId);

        Long remain = redis.opsForSet()
                .size("user:" + userId + ":conns");

        // 마지막 연결 종료 → 오프라인
        if (remain == null || remain == 0) {
            redis.opsForHash()
                    .delete(ONLINE_USERS, userId);
            broadcast();
        }
    }
    public Set<OnlineStatusDto> getOnlineUsers() {
        return redis.opsForHash()
                .entries(ONLINE_USERS)
                .entrySet()
                .stream()
                .map(e ->
                        new OnlineStatusDto(
                                (String) e.getKey(),
                                (String) e.getValue(),
                                true
                        )
                )
                .collect(Collectors.toSet());
    }

    private void broadcast() {
        messaging.convertAndSend(
                "/topic/online-users",
                getOnlineUsers()
        );
    }
}
