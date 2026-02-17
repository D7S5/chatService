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
    public void broadcastJoin(
            String roomId,
            ParticipantDto dto) {
        messagingTemplate.convertAndSend(
                "/topic/rooms/" + roomId + "/participants",
                new ParticipantEvent(
                        ParticipantEventType.JOIN,
                        roomId,
                        dto,
                        null
                )
        );
    }

    @Override
    public void broadcastLeave(
            String roomId,
            ParticipantDto dto
    ) {
        messagingTemplate.convertAndSend(
                "/topic/rooms/" + roomId + "/participants",
                new ParticipantEvent(
                        ParticipantEventType.LEAVE,
                        roomId,
                        dto,
                        null
                )
        );
    }

    @Override
    public void broadcastLeave(
            String roomId,
            ParticipantDto dto,
            String reason
    ) {
        messagingTemplate.convertAndSend(
                "/topic/rooms/" + roomId + "/participants",
                new ParticipantEvent(
                        ParticipantEventType.LEAVE,
                        roomId,
                        dto,
                        reason
                )
        );
        if (reason != null) {
            messagingTemplate.convertAndSendToUser(
                    dto.getUserId(),
                    "/queue/room-force-exit",
                    new RoomForceExitDto(roomId, reason)
            );
        }
    }

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
