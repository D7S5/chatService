package com.example.chatservice.component;

import com.example.chatservice.dto.UserEnterDto;
import com.example.chatservice.entity.User;
import com.example.chatservice.redis.OnlineStatusService;
import com.example.chatservice.redis.OnlineStatusServiceV2;
import com.example.chatservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;

@Component
@RequiredArgsConstructor
public class WebSocketConnectHandler implements ApplicationListener<SessionConnectEvent> {

    private final OnlineStatusService onlineStatusService;
    private final UserRepository userRepository;

    @Override
    public void onApplicationEvent(SessionConnectEvent event) {

        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());

        // HandshakeInterceptor 에서 저장된 값
        var sessionAttrs = accessor.getSessionAttributes();

        String sessionId = accessor.getSessionId();

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