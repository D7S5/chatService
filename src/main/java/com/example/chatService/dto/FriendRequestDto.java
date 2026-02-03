package com.example.chatService.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FriendRequestDto {
    private Long id;
    private String fromUserId;
    private String fromUserNickname;
    private String toUserId;
    private FriendStatus status;
}