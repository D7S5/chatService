package com.example.chatService.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupMessageDto {

    private String roomId; // partition key
    private String senderId;
    private String senderName;
    private String content;
    private long sentAt; // epoch millis
}
