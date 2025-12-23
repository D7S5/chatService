package com.example.chatservice.dto;

import lombok.*;

@Getter @Setter
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
public class GroupMessage {

    private String roomId;      // partition key
    private String senderId;
    private String senderName;
    private String content;
    private Long sentAt;   // epoch millis
}
