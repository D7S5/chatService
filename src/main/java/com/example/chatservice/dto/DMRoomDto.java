package com.example.chatservice.dto;

public record DMRoomDto(
        String roomId,
        String targetUserId,
        String targetUsername,
        int unreadCount) {
}


