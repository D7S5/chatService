package com.example.chatService.event;

import lombok.Getter;

@Getter
public class ParticipantForcedExitEvent {

    private final String roomId;
    private final String userId;
    private final String reason;

    public ParticipantForcedExitEvent(String roomId, String userId, String reason) {
        this.roomId = roomId;
        this.userId = userId;
        this.reason = reason;
    }
}
