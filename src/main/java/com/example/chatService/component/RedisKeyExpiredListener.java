package com.example.chatService.component;

import com.example.chatService.dto.UserRoomSession;
import com.example.chatService.redis.UserSessionRegistry;
import com.example.chatService.service.RoomParticipantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RedisKeyExpiredListener implements MessageListener {

    private static final String ONLINE_TTL_KEY_PREFIX = "online:ttl:";

    private final RoomParticipantService roomParticipantService;
    private final UserSessionRegistry userSessionRegistry;

    // Redis 키 만료시 이벤트
    @Override
    public void onMessage(Message message, byte[] pattern) {
        String expiredKey = new String(message.getBody());

        if (expiredKey.startsWith(ONLINE_TTL_KEY_PREFIX)) {
            String userId = expiredKey.replace(ONLINE_TTL_KEY_PREFIX, "");
            log.info("❌ User offline => {}", userId);
            return;
        }

        if (!userSessionRegistry.isTtlKey(expiredKey)) {
            return;
        }

        UserRoomSession info = userSessionRegistry.removeByTtlKey(expiredKey);
        if (info == null) {
            return;
        }

        try {
            roomParticipantService.leaveRoom(info.getRoomId(), info.getUserId());
            log.info("expired room session cleaned roomId={}, userId={}", info.getRoomId(), info.getUserId());
        } catch (Exception e) {
            log.warn("expired room session cleanup failed roomId={}, userId={}", info.getRoomId(), info.getUserId(), e);
        }
    }
}
