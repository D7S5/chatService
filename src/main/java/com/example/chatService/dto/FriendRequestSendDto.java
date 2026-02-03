package com.example.chatService.dto;

import lombok.Data;

@Data
public class FriendRequestSendDto {
    private String fromUserId;
    private String toUserId;
}