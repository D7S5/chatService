package com.example.chatservice.component;

import com.example.chatservice.redis.OnlineStatusService;
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

//    @EventListener
//    public void handleSessionConnected(SessionConnectEvent event) {
//        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
//        String userId = getUserIdFromAccessor(accessor);
//        if (userId != null) {
//            onlineStatusService.markOnline(userId);
//        }
//    }

    @EventListener
    public void handleSessionDisconnected(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String userId = getUserIdFromAccessor(accessor);
        if (userId != null) {
            onlineStatusService.markOffline(userId);
        }
    }

    private String getUserIdFromAccessor(StompHeaderAccessor accessor) {
        Principal user = accessor.getUser();
        return user != null ? user.getName() : null;
    }

    private String getUserIdFromSession(StompHeaderAccessor accessor) {
        // 방법 1: Principal에서 가져오기 (JWT 인증 시)
        Principal principal = accessor.getUser();
        if (principal != null) {
            return principal.getName(); // username 또는 userId
        }

        // 방법 2: 세션 속성에서 가져오기 (인증 인터셉터에서 넣어둔 경우)
        Map<String, Object> sessionAttributes = accessor.getSessionAttributes();
        if (sessionAttributes != null) {
            Object userObj = sessionAttributes.get("userId");
            if (userObj instanceof String) {
                return (String) userObj;
            }
        }

        return null;
    }

    // 이벤트용 오버로드 (편의 메서드)
    private String getUserIdFromSession(ApplicationEvent event) {
        if (event instanceof SessionConnectEvent) {
            return getUserIdFromSession(StompHeaderAccessor.wrap(((SessionConnectEvent) event).getMessage()));
        } else if (event instanceof SessionDisconnectEvent) {
            return getUserIdFromSession(StompHeaderAccessor.wrap(((SessionDisconnectEvent) event).getMessage()));
        }
        return null;
    }
}