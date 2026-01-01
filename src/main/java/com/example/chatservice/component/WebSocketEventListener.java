package com.example.chatservice.component;

import com.example.chatservice.redis.OnlineStatusServiceV3;
import com.example.chatservice.service.ChatRoomV2Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketEventListener {

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
                (String) accessor.getSessionAttributes().get("userId");

        if (roomId != null) {
            chatRoomV2Service.leaveBySession(roomId, sessionId);
//            System.out.println("Leave userId = " + userId + " roomId = " + roomId); // debug
        }
    }
}