package com.example.chatservice.dto;


import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ChatMessageDto {
    private String type; // CHAT, JOIN, LEAVE...
    private String roomId;
    private String sender;
    private String senderName;
    private String content;
    private String sentAt; // optional
}