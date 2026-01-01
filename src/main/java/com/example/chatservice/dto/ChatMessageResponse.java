package com.example.chatservice.dto;

import com.example.chatservice.entity.GroupMessageEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class ChatMessageResponse {

    private String roomId;
    private String senderId;
    private String senderName;
    private String content;
    private long createdAt;

    public static ChatMessageResponse from(GroupMessageEntity e) {
        return ChatMessageResponse.builder()
                .roomId(e.getRoomId())
                .senderId(e.getSenderId())
                .senderName(e.getSenderName())
                .content(e.getContent())
                .createdAt(e.getCreatedAt().toInstant().toEpochMilli())
                .build();

    }
}