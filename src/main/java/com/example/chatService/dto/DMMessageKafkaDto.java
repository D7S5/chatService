package com.example.chatService.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DMMessageKafkaDto {
    private String roomId;
    private String senderId;
    private String content;
    private long sentAt;
}