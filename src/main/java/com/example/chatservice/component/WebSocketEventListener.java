package com.example.chatservice.component;

import com.example.chatservice.redis.OnlineStatusService;
import com.example.chatservice.service.ChatRoomV2Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketEventListener {

    private final OnlineStatusService onlineStatusService;
    private final ChatRoomV2Service chatRoomV2Service;
    private final StringRedisTemplate redis;

    @EventListener
    public void handleSessionDisconnected(SessionDisconnectEvent event) {

        StompHeaderAccessor accessor =
                StompHeaderAccessor.wrap(event.getMessage());

        String sessionId = accessor.getSessionId();
        if (sessionId == null) return;

        String roomId =
                redis.opsForValue().get("session:" + sessionId + ":room");
        String userId =
                redis.opsForValue().get("session:" + sessionId + ":user");

        if (userId != null) {
            onlineStatusService.removeSession(sessionId);
        }

        if (roomId != null && userId != null) {
            chatRoomV2Service.leaveBySession(roomId, sessionId);
        }

        redis.delete(
                "session:" + sessionId + ":user");
        redis.delete(
                "session:" + sessionId + ":room");

        log.info("WS disconnect → leave room={}, user={}, session={}",
                roomId, userId, sessionId);
    }
}