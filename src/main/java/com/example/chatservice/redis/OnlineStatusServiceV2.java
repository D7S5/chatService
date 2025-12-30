package com.example.chatservice.redis;

import com.example.chatservice.dto.OnlineStatusDto;
import com.example.chatservice.dto.UserEnterDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OnlineStatusServiceV2 {

    private final StringRedisTemplate redisTemplate;
    private final SimpMessagingTemplate messagingTemplate;

    private static final String ONLINE_HASH = "online:users";
    private static final Duration EXPIRE_MINUTES = Duration.ofMinutes(1);

    /**
     * 유저 온라인 등록
     */
    public void markOnline(UserEnterDto dto, String sessionId) {
        String userId = dto.getUserId();
        String sessionSetKey = "user:" + userId + ":sessions";

        Set<String> sessions =
                redisTemplate.opsForSet().members(sessionSetKey);

        if (sessions != null) {
            for (String oldSessionId : sessions) {
                String ttlKey = "session:" + oldSessionId + ":ttl";
                if (!Boolean.TRUE.equals(redisTemplate.hasKey(ttlKey)))
                    redisTemplate.opsForSet()
                            .remove(sessionSetKey, oldSessionId);
            }
        }

        redisTemplate.opsForValue()
                        .set("session:" + sessionId + ":user", userId);

        // 에러지점
        redisTemplate.opsForSet()
                        .add("user:" + userId + ":sessions", sessionId);

        redisTemplate.opsForValue()
                        .set("session:" + sessionId + ":ttl","1", EXPIRE_MINUTES);

        Long size = redisTemplate.opsForSet()
                        .size("user:" + userId + ":sessions");

        System.out.println("size = " + size);

        if (size != null && size == 1) {
            redisTemplate.opsForHash().put(ONLINE_HASH, userId, dto.getUsername());
        }
        broadcastOnlineUsers();
    }

    public void refreshTTL(String sessionId) {
        // userId 지울지
        redisTemplate.expire("session:" + sessionId + ":ttl", EXPIRE_MINUTES);
    }

    /**
     * 유저 오프라인 처리
     */
    public void markOffline(String sessionId) {
        String redisUserId =
                redisTemplate.opsForValue().get("session:" + sessionId + ":user");

        if (redisUserId == null) return;

        redisTemplate.delete("session:" + sessionId + ":ttl");
        redisTemplate.delete("session:" + sessionId + ":user");

        redisTemplate.opsForSet()
                        .remove("user:" + redisUserId + ":sessions", sessionId);

        Long remain = redisTemplate.opsForSet()
                        .size("user:" + redisUserId + ":sessions");

        if (remain == null || remain == 0) {
            redisTemplate.opsForHash().delete(ONLINE_HASH, redisUserId);
        }
        broadcastOnlineUsers();
    }

    public Set<OnlineStatusDto> getAllOnlineUsers() {
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(ONLINE_HASH);

        return entries
                .entrySet()
                .stream()
                .map(e -> new OnlineStatusDto((String) e.getKey(), (String) e.getValue(), true))
                .collect(Collectors.toSet());
    }

    public void broadcastOnlineUsers() {
        Set<OnlineStatusDto> set = getAllOnlineUsers();
        messagingTemplate.convertAndSend("/topic/online-users", set);
    }

//    @Scheduled(fixedRate = 5000)
//    public void cleanDeadConnections() {
//        Set<String> sessionKeys = redisTemplate.keys("session:*:user");
//
//        for (String key : sessionKeys) {
//            String sessionId = key.split(":")[1];
//            String ttlKey = "session:" + sessionId + ":ttl";
//
//            if (!Boolean.TRUE.equals(redisTemplate.hasKey(ttlKey)))
//                markOffline(sessionId);
//        }
//    }

//    @Scheduled(fixedRate = 5000)
//    public void cleanExpiredUsersV2() {
//        Set<Object> allUsers = redisTemplate.opsForHash().keys(ONLINE_HASH);
//
//        boolean changed = false;
//
//        for (Object userIdObj : allUsers) {
//            String userId = userIdObj.toString();
//            String ttlKey = TTL_KEY_PREFIX + userId;
//
//            if (!Boolean.TRUE.equals(redisTemplate.hasKey(ttlKey))) {
//                redisTemplate.opsForHash().delete(ONLINE_HASH, userId);
//
//                changed = true;
//            }
//        }
//
//        if (changed) {
//            Map<Object, Object> onlineUsers = redisTemplate.opsForHash().entries(ONLINE_HASH);
//
//            messagingTemplate.convertAndSend(
//                    "/topic/online-users",
//                    onlineUsers
//            );
//        }
//    }
}