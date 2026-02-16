package com.example.chatService.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class PublishAcceptFriendEvent {
    private String type;
    private String friendId;
}
