package com.example.chatService.entity;

import com.example.chatService.dto.GroupMessageDto;
import jakarta.persistence.*;
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

    @Column(name = "sender_name", nullable = false)
    private String senderName;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    public static GroupMessageEntity from(GroupMessageDto message) {
        GroupMessageEntity e = new GroupMessageEntity();
        e.roomId = message.getRoomId();
        e.senderId = message.getSenderId();
        e.senderName = message.getSenderName();
        e.content = message.getContent();
        e.createdAt = OffsetDateTime.ofInstant(
                Instant.ofEpochMilli(message.getSentAt()),
                ZoneOffset.UTC
        );
        return e;
    }
}
