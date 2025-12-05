package com.example.chatservice.component;

import com.example.chatservice.entity.User;
import com.example.chatservice.redis.OnlineStatusService;
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
        if (sessionAttrs == null) {
            System.out.println("SessionAttributes is NULL");
            return;
        }

        String userId = (String) sessionAttrs.get("userId");

        User user = userRepository.findById(userId).orElse(null);
        String username = user != null ? user.getUsername() : null;

//        System.out.println("Connected UserId: " + userId + " username: " + username); debug

        if (userId != null) {
            onlineStatusService.markOnline(userId, username);
        }
    }
}
