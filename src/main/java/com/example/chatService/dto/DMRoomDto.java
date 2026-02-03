package com.example.chatService.dto;

public record DMRoomDto(
        String roomId,
        String targetUserId,
        String targetUsername,
        int unreadCount) {
}


