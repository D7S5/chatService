package com.example.chatservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor(force = true)
@AllArgsConstructor
public class GroupMessage {

    private String roomId;      // partition key
    private String senderId;
    private String senderName;
    private String content;

    private Long createdAt;   // epoch millis
}
