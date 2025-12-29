package com.example.chatservice.component;

import com.example.chatservice.dto.UserDto;
import com.example.chatservice.dto.UserEnterDto;
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

    @Override
    public void onApplicationEvent(SessionConnectEvent event) {

        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());

        var sessionAttrs = accessor.getSessionAttributes();
        if (sessionAttrs == null) return;

        String userId = (String) sessionAttrs.get("userId");
        String username = (String) sessionAttrs.get("username");
        String sessionId = accessor.getSessionId();

        if (userId == null && sessionId == null) return;

        onlineStatusService.addSession(userId, username, sessionId);
    }
}