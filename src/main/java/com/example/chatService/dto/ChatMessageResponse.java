package com.example.chatService.dto;

import com.example.chatService.entity.GroupMessageEntity;
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
    private ChatMessageType messageType;
    private String imageUrl;
    private long sentAt;

    public static ChatMessageResponse from(GroupMessageEntity e) {
        return ChatMessageResponse.builder()
                .roomId(e.getRoomId())
                .senderId(e.getSenderId())
                .senderName(e.getSenderName())
                .content(e.getContent())
                .messageType(e.getMessageType())
                .imageUrl(e.getImageUrl())
                .sentAt(e.getCreatedAt().toInstant().toEpochMilli())
                .build();

    }
}
