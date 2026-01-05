package com.example.chatservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OwnerChangedEvent {
    private final String roomId;
    private final String newOwnerId;
}
