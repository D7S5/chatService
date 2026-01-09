package com.example.chatservice.component;

import lombok.RequiredArgsConstructor;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class WebSocketEventPublisher {

    private final SimpMessagingTemplate messagingTemplate;

    public void publishFriendEvent(String userId, Map<String, Object> payload) {
        messagingTemplate.convertAndSend("/topic/friends/" + userId, payload);
    }

    @Component
    public static class CustomHandshakeHandler extends DefaultHandshakeHandler {

        @Override
        protected Principal determineUser(ServerHttpRequest request,
                                          WebSocketHandler wsHandler,
                                          Map<String, Object> attributes) {

            String userId = (String) attributes.get("userId");

            // Principal.getName() == userId
            return () -> userId;
        }
    }
}


