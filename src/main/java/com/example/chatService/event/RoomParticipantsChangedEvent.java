package com.example.chatService.event;

import lombok.Getter;

@Getter
public class RoomParticipantsChangedEvent {

    private final String roomId;

    public RoomParticipantsChangedEvent(String roomId) {
        this.roomId = roomId;
    }
}
