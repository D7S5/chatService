package com.example.chatservice.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ChatMessageResponse {

    private String roomId;
    private String senderId;
    private String content;
    private long createdAt;

    public static ChatMessageResponse from(GroupMessageEntity e) {
        return new ChatMessageResponse(
                e.getRoomId(),
                e.getSenderId(),
                e.getContent(),
                e.getCreatedAt().toInstant().toEpochMilli()
        );
    }
}
