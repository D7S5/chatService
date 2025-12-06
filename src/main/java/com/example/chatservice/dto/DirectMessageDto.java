package com.example.chatservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DirectMessageDto {
    private String roomId;
    private String senderId;
    private String receiverId;
    private String content;
    private long timestamp;
}
