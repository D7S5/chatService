package com.example.chatservice.component;

import com.example.chatservice.redis.OnlineStatusService;
import com.example.chatservice.service.ChatRoomV2Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.EventListener;
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

    @EventListener
    public void handleSessionDisconnected(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
//        String userId = getUserIdFromAccessor(accessor);

        Map<String, Object> session =
                accessor.getSessionAttributes();

        if (session == null) return;

        String userId = (String) session.get("userId");
        String roomId = (String) session.get("roomId");

        if (userId != null) {
            onlineStatusService.markOffline(userId);
        }

        if (userId != null && roomId != null) {
            chatRoomV2Service.leave(roomId, userId);
            log.info("WS disconnect → leave room={}, user={}", roomId, userId);
        }
    }
}