package com.example.chatservice.dto;

import com.example.chatservice.entity.Friend;

public record FriendDto(
        Long id,
        String username,
        String friendUsername,
        FriendStatus status) {
}
