package com.example.chatService.component;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class WebSocketEventPublisher {

    private final SimpMessagingTemplate messagingTemplate;

    public void publishFriendEvent(String userId, Map<String, Object> payload) {
        messagingTemplate.convertAndSend("/topic/friends/" + userId, payload);
    }
}