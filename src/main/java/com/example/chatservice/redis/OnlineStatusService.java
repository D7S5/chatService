package com.example.chatservice.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class OnlineStatusService {

    private final StringRedisTemplate redis;
    private final SimpMessagingTemplate messagingTemplate;

    public void addSession(String userId, String username, String sessionId) {
        String sessionKey = "session:" + sessionId + ":user";
        String existingUser = redis.opsForValue().get(sessionKey);

        if (existingUser != null && !existingUser.equals(userId)) {
            throw new IllegalStateException("Session already bound to another user");
        }

        redis.opsForValue().set(sessionKey, userId);

        Long size = redis.opsForSet()
                .add("user:" + userId + ":sessions", sessionId);

        if (size != null && size == 1) {
            redis.opsForValue().set("user:" + userId + ":online", "1");
            notifyFriends(userId, true);
//            broadcastLobbyOnlineCount();
        }
    }

    public void removeSession(String sessionId) {
        String sessionKey = "session:" + sessionId + ":user";
        String userId = redis.opsForValue().get(sessionKey);

        if (userId == null) return;

        redis.delete(sessionKey);

        redis.opsForSet()
                .remove("user:" + userId + ":sessions", sessionId);

        Long remain = redis.opsForSet()
                .size("user:" + userId + ":sessions");

        if (remain == null || remain == 0) {
            redis.delete("user:" + userId + ":online");
            notifyFriends(userId, false);
//            broadcastLobbyOnlineCount();
        }
    }

    public void notifyFriends(String userId, boolean online) {

        Set<String> friends =
                redis.opsForSet().members("user:" + userId + ":friends");

        if ( friends == null || friends.isEmpty()) return;

        Map<String, Object> payload = Map.of(
                "userId", userId,
                "online", online
        );

        for (String friendId : friends) {
            messagingTemplate.convertAndSend(
                    "/topic/friends/status/" + friendId,
                    payload
            );
        }
    }

//    private void broadcastLobbyOnlineCount() {
//        Set<String> keys = redis.keys("user:*:sessions");
//        if (keys == null) return;
//
//        int onlineCount = (int) keys.stream()
//                .filter(k -> {
//                    Long size = redis.opsForSet().size(k);
//                    return size != null && size > 0;
//                })
//                .count();
//
//        messagingTemplate.convertAndSend(
//                "/topic/lobby/online-count",
//                Map.of("count", onlineCount)
//        );
//    }
}