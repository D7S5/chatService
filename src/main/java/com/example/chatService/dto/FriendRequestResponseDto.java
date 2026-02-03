package com.example.chatService.dto;

import lombok.Builder;
import lombok.Data;

@Data @Builder
public class FriendRequestResponseDto {
    private String fromUserId;
    private String toUserId;
    private String status;
}