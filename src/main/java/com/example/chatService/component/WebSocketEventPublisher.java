package com.example.chatService.component;

import com.example.chatService.dto.PublishAcceptFriendEvent;
import com.example.chatService.dto.PublishFriendEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class WebSocketEventPublisher {

    private final SimpMessagingTemplate messagingTemplate;

    public void publishFriendEvent(String userId, PublishFriendEvent publishFriendEvent) {
        messagingTemplate.convertAndSend("/topic/friends/" + userId, publishFriendEvent);
    }

    public void publishAcceptFriendEvent(String userId, PublishAcceptFriendEvent publishFriendEvent) {
        messagingTemplate.convertAndSend("/topic/friends/" + userId, publishFriendEvent);
    }
}