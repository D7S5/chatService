package com.example.chatservice.dto;

import com.example.chatservice.entity.DMMessage;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DMMessageResponse {
    private Long messageId;
    private String roomId;
    private String senderId;
    private String content;
    private String sentAt;

    public static DMMessageResponse from(DMMessage message) {
        return DMMessageResponse.builder()
                .messageId(message.getId())
                .roomId(message.getRoom().getRoomId())
                .senderId(message.getSenderId())
                .content(message.getContent())
                .sentAt(message.getSentAt().toString())
                .build();
    }
}
