package com.example.chatService.event;

import lombok.Getter;

@Getter
public class RoomParticipantsChangedEvent {
    private final String roomId;
    private final int currentCount;
    private final int maxParticipants;

    public RoomParticipantsChangedEvent(String roomId, int currentCount, int maxParticipants) {
        this.roomId = roomId;
        this.currentCount = currentCount;
        this.maxParticipants = maxParticipants;
    }
}
