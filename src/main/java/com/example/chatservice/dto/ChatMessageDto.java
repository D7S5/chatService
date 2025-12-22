package com.example.chatservice.dto;


import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter @Setter
public class ChatMessageDto {
    private String type; // CHAT, JOIN, LEAVE...
    private String roomId;
    private String sender;
    private String senderName;
    private String content;
    private OffsetDateTime createdAt; // optional

    public ChatMessageDto(String roomId, String sender, String senderName, String content, OffsetDateTime createdAt) {
        this.roomId = roomId;
        this.sender = sender;
        this.senderName = senderName;
        this.content = content;
        this.createdAt = createdAt;
    }
}