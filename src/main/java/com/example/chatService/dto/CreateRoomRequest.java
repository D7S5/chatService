package com.example.chatService.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CreateRoomRequest {

    private String name;
    private int maxParticipants;
    private RoomType type;
}
