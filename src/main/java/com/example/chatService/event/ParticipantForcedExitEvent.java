package com.example.chatService.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ParticipantForcedExitEvent {

    private final String roomId;
    private final String userId;
    private final String reason;

}
