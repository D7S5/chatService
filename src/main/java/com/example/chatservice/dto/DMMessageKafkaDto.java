package com.example.chatservice.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DMMessageKafkaDto {
    private String roomId;
    private String senderId;
    private String content;
    private long timestamp;
 }
