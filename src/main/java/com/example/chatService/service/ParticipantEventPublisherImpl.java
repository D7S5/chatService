package com.example.chatService.service;

import com.example.chatService.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class ParticipantEventPublisherImpl implements ParticipantEventPublisher {

    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public void broadcastOwnerChanged(String roomId, String newOwnerId) {
        messagingTemplate.convertAndSend(
                "/topic/room/" + roomId + "/owner",
                new OwnerChangedEvent(roomId, newOwnerId)
        );

        log.info(
                "[OWNER_CHANGED] roomId={}, newOwnerId={}",
                roomId, newOwnerId
        );
    }
}
