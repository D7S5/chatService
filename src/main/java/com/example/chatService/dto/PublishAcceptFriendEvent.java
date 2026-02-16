package com.example.chatService.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class PublishAcceptFriendEvent {
    private FriendEventType type;
    private String friendId;
}
