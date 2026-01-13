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
    private int currentCount;
    private int maxParticipants;
    private boolean largeRoom;
    private String ownerUserId;
    private String inviteToken; // PRIVATE일 때만

    public static RoomResponse from(ChatRoomV2 r) {
        return new RoomResponse(
                r.getRoomId(),
                r.getName(),
                r.getType(),
                r.getCurrentCount(),
                r.getMaxParticipants(),
                r.isLargeRoom(),
                r.getOwnerUserId(),
                null
        );
    }

    public static RoomResponse of(ChatRoomV2 room, String inviteToken) {
        return new RoomResponse(
                room.getRoomId(),
                room.getName(),
                room.getType(),
                room.getCurrentCount(),
                room.getMaxParticipants(),
                room.isLargeRoom(),
                room.getOwnerUserId(),
                inviteToken
        );
    }
}