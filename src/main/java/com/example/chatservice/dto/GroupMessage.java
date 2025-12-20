package com.example.chatservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;
import java.time.OffsetDateTime;

@Getter
@AllArgsConstructor
public final class GroupMessage {

    private final String roomId;      // partition key
    private final String senderId;
    private final String content;
    private final OffsetDateTime createdAt;   // epoch millis

    public static GroupMessage of(String roomId,
                                  String senderId,
                                  String content) {
        return new GroupMessage(
                roomId,
                senderId,
                content,
                OffsetDateTime.now()
        );
    }

}
