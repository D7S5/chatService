package com.example.chatService.component;

import com.example.chatService.dto.UserEnterDto;
import com.example.chatService.entity.User;
import com.example.chatService.redis.OnlineStatusService;
import com.example.chatService.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;

@Component
@Slf4j
@RequiredArgsConstructor
public class WebSocketConnectHandler implements ApplicationListener<SessionConnectEvent> {

    private final OnlineStatusService onlineStatusService;
    private final UserRepository userRepository;

    @Override
    public void onApplicationEvent(SessionConnectEvent event) {

        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());

        var sessionAttrs = accessor.getSessionAttributes();

        String sessionId = accessor.getSessionId();

        if (sessionAttrs == null) {
            log.warn("Session attributes is null. sessionId={}", sessionId);
            return;
        }

        String userId = (String) sessionAttrs.get("userId");
        if (userId == null) {
            System.out.println("WebSocket Connect: userId is NULL (HandshakeInterceptor failed)");
            return;
        }

        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            System.out.println("WebSocket Connect: userId " + userId + " not found in DB");
            return;
        }
        String username = user.getUsername();

        UserEnterDto dto = new UserEnterDto(userId, username);

        if (userId != null) {
            onlineStatusService.markOnline(dto);
        }
    }
}