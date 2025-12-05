package com.example.chatservice.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class DMMessageRequest {
    private String roomId;
    private String senderId;
    private String content;
}