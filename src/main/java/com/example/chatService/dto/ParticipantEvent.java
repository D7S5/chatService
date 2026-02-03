package com.example.chatService.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ParticipantEvent {

    private ParticipantEventType type;   // JOIN / LEAVE
    private String roomId;
    private ParticipantDto participant;
    private String reason;   // KICK, TIMEOUT
}