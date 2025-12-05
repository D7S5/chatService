package com.example.chatservice.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class PrivateMessageDto {
    private String sender;
    private String receiver;
    private String content;
    private String type;
}
