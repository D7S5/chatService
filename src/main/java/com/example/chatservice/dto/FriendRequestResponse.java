package com.example.chatservice.dto;


import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter @Builder
public class FriendRequestResponse {
    private Long id;
    private String fromUserId;
    private String fromUserNickname;
    private FriendStatus status;
}
