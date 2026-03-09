package com.example.chatService.component;

import com.example.chatService.dto.UserRoomSession;
import com.example.chatService.redis.UserSessionRegistry;
import com.example.chatService.service.RoomParticipantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketDisconnectListener {

    private final RoomParticipantService roomParticipantService;
    private final UserSessionRegistry userSessionRegistry;

    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());

        String sessionId = accessor.getSessionId();

        UserRoomSession info = userSessionRegistry.remove(sessionId);
        if (info == null) return;

        try {
            roomParticipantService.leaveRoom(info.getRoomId(), info.getUserId());
        } catch (Exception e) {
            log.warn("disconnect leave failed roomId={}, userId={}", info.getRoomId(), info.getUserId(), e);
        }
    }
}