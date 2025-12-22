package com.example.chatservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.OffsetDateTime;

@Getter
@AllArgsConstructor
public class GroupMessageDto {

    private String roomId;
    private String senderId;
    private String senderName;
    private String content;
    private OffsetDateTime createdAt;
}
