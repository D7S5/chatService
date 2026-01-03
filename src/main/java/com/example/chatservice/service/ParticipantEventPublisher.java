package com.example.chatservice.service;

import com.example.chatservice.dto.ParticipantDto;
import com.example.chatservice.dto.ParticipantEvent;
import com.example.chatservice.dto.ParticipantEventType;
import com.example.chatservice.entity.RoomParticipant;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ParticipantEventPublisher {

    private final SimpMessagingTemplate messagingTemplate;

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
}
