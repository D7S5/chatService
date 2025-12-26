package com.example.chatservice.dto;

import com.example.chatservice.entity.ChatRoomV2;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@AllArgsConstructor
@ToString
public class RoomResponse {

    private String roomId;
    private String name;
    private RoomType type;
    private int maxParticipants;
    private int currentCount;
    private boolean largeRoom;

    public static RoomResponse create(ChatRoomV2 r) {
        return new RoomResponse(
                r.getRoomId(),
                r.getName(),
                r.getType(),
                r.getMaxParticipants(),
                0,
                r.isLargeRoom()
        );
    }
    public static RoomResponse from(ChatRoomV2 r, int currentCount) {
        return new RoomResponse(
                r.getRoomId(),
                r.getName(),
                r.getType(),
                r.getMaxParticipants(),
                currentCount,
                r.isLargeRoom()
        );
    }
}