package com.example.chatservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.OffsetDateTime;

@Getter
@AllArgsConstructor
public class GroupMessageWsDto {

    private long messageId;
    private String roomId;
    private String senderId;
    private String content;
    private OffsetDateTime createdAt;
}
