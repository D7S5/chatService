package com.example.chatservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class ParticipantEvent {

    private ParticipantEventType type;   // JOIN / LEAVE
    private String roomId;
    private ParticipantDto participant;
    private String reason;   // KICK, TIMEOUT
}