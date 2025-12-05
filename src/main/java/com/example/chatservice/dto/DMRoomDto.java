package com.example.chatservice.dto;

import java.util.List;

public record DMRoomDto(
        String roomId,
        String targetUserId,
        String targetUsername,
        int unreadCount) {
}


