package com.example.chatService.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DirectMessageDto {
    private String roomId;
    private String senderId;
    private String content;
    private LocalDateTime timestamp;
}
