package com.example.chatservice.dto;

import com.example.chatservice.entity.Friend;
import com.example.chatservice.entity.FriendRequest;
import lombok.Builder;
import lombok.Data;
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