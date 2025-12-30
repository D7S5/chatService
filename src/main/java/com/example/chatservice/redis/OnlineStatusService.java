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
public class OnlineStatusService {

    private final StringRedisTemplate redisTemplate;
    private final SimpMessagingTemplate messagingTemplate;

    private static final String ONLINE_HASH = "online:users";
    private static final String TTL_KEY_PREFIX = "online:ttl:";
    private static final Duration EXPIRE_MINUTES = Duration.ofMinutes(2);

    /**
     * 유저 온라인 등록
     */
    public void markOnline(UserEnterDto dto) {
        String userKey = dto.getUserId();

        redisTemplate.opsForHash().put(ONLINE_HASH, userKey, dto.getUsername());
        String ttlKey = TTL_KEY_PREFIX + userKey;
        redisTemplate.opsForValue().set(ttlKey, "1", EXPIRE_MINUTES);

        broadcastOnlineUsers();
    }

    public void refreshTTL(String userId) {
        String key = TTL_KEY_PREFIX + userId;
//        System.out.println("TTL 갱신 " + userId);  // debug
        redisTemplate.expire(key, EXPIRE_MINUTES);
    }

    /**
     * 유저 오프라인 처리
     */
    public void markOffline(String userId) {
        redisTemplate.opsForHash().delete(ONLINE_HASH, userId);
        redisTemplate.delete(TTL_KEY_PREFIX + userId);

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

    @Scheduled(fixedRate = 5000)
    public void cleanExpiredUsersV2() {
        Set<Object> allUsers = redisTemplate.opsForHash().keys(ONLINE_HASH);

        boolean changed = false;

        for (Object userIdObj : allUsers) {
            String userId = userIdObj.toString();
            String ttlKey = TTL_KEY_PREFIX + userId;

            if (!Boolean.TRUE.equals(redisTemplate.hasKey(ttlKey))) {
                redisTemplate.opsForHash().delete(ONLINE_HASH, userId);

                changed = true;
            }
        }

        if (changed) {
            Map<Object, Object> onlineUsers = redisTemplate.opsForHash().entries(ONLINE_HASH);

            messagingTemplate.convertAndSend(
                    "/topic/online-users",
                    onlineUsers
            );
        }
    }
}