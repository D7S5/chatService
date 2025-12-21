package com.example.chatservice.dto;

import com.example.chatservice.entity.ChatRoomV2;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RoomResponse {

    private String roomId;
    private String name;
    private RoomType type;
    private int maxParticipants;
    private boolean largeRoom;

    public static RoomResponse from(ChatRoomV2 r) {
        return new RoomResponse(
                r.getRoomId(),
                r.getName(),
                r.getType(),
                r.getMaxParticipants(),
                r.isLargeRoom()
        );
    }
}