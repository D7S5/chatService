package com.example.chatService.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RoomForceExitDto {
    private String roomId;
    private String reason;
}
