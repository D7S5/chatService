package com.example.chatservice.service;

import com.example.chatservice.dto.OwnerChangedEvent;
import com.example.chatservice.dto.ParticipantDto;
import com.example.chatservice.dto.ParticipantEvent;
import com.example.chatservice.dto.ParticipantEventType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class ParticipantEventPublisherImpl implements ParticipantEventPublisher{

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
                        new ParticipantDto(
                                dto.getUserId(),
                                dto.getUsername(),
                                dto.getRole()
                        ), null
                )
        );
    }
    @Override
    public void broadcastLeave(
            String roomId,
            ParticipantDto participant
    ) {
        messagingTemplate.convertAndSend(
                "/topic/rooms/" + roomId + "/participants",
                new ParticipantEvent(
                        ParticipantEventType.LEAVE,
                        roomId,
                        participant,
                        null
                )
        );
    }
    @Override
    public void broadcastLeave(
            String roomId,
            ParticipantDto participant,
            String reason
    ) {
        messagingTemplate.convertAndSend(
                "/topic/rooms/" + roomId + "/participants",
                new ParticipantEvent(
                        ParticipantEventType.LEAVE,
                        roomId,
                        participant,
                        reason
                )
        );
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
