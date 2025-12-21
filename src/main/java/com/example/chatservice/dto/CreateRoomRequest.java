package com.example.chatservice.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CreateRoomRequest {

    private String name;
    private RoomType type;
    private int maxParticipants;
}
