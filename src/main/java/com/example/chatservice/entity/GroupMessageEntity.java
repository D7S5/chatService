package com.example.chatservice.entity;

import com.example.chatservice.dto.GroupMessage;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Entity
@Table(name = "group_messages")
@Getter
@NoArgsConstructor
public class GroupMessageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "room_id", nullable = false)
    private String roomId;

    @Column(name = "sender_id", nullable = false)
    private String senderId;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    public static GroupMessageEntity from(GroupMessage message) {
        GroupMessageEntity e = new GroupMessageEntity();
        e.roomId = message.getRoomId();
        e.senderId = message.getSenderId();
        e.content = message.getContent();
        e.createdAt = OffsetDateTime.ofInstant(
                Instant.ofEpochMilli(message.getCreatedAt()),
                ZoneOffset.UTC
        );
        return e;
    }
}
