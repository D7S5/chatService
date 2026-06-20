package com.example.chatService.entity;

import com.example.chatService.dto.GroupMessageDto;
import com.example.chatService.dto.ChatMessageType;
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

    @Column(columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "message_type", nullable = false, length = 10,
            columnDefinition = "varchar(10) default 'TEXT'")
    private ChatMessageType messageType;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    public static GroupMessageEntity from(GroupMessageDto message) {
        GroupMessageEntity e = new GroupMessageEntity();
        e.roomId = message.getRoomId();
        e.senderId = message.getSenderId();
        e.senderName = message.getSenderName();
        e.content = message.getContent();
        e.messageType = message.getMessageType() == null ? ChatMessageType.TEXT : message.getMessageType();
        e.imageUrl = message.getImageUrl();
        e.createdAt = OffsetDateTime.ofInstant(
                Instant.ofEpochMilli(message.getSentAt()),
                ZoneOffset.UTC
        );
        return e;
    }
}
