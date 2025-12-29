package com.example.chatservice.dto;

import com.example.chatservice.entity.GroupMessageEntity;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserEnterDto {
    private String userId;
    private String username;

    @Getter
    @AllArgsConstructor
    @Builder
    public static class ChatMessageResponse {

        private String roomId;
        private String senderId;
        private String senderName;
        private String content;
        private long createdAt;

        public static ChatMessageResponse from(GroupMessageEntity e) {
            return ChatMessageResponse.builder()
                    .roomId(e.getRoomId())
                    .senderId(e.getSenderId())
                    .senderName(e.getSenderName())
                    .content(e.getContent())
                    .createdAt(e.getCreatedAt().toInstant().toEpochMilli())
                    .build();

        }
    }
}