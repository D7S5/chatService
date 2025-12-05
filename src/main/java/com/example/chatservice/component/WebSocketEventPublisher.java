package com.example.chatservice.component;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class WebSocketEventPublisher {

    private final SimpMessagingTemplate messagingTemplate;

    /** 친구 관련 이벤트 발행 */
    public void publishFriendEvent(String userId, Map<String, Object> payload) {
        messagingTemplate.convertAndSend("/topic/friends/" + userId, payload);
    }
}


