package com.example.chatservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupMessageDto {

    private String roomId;
    private String senderId;
    private String senderName;
    private String content;
    private long sentAt;
}
