package com.example.chatservice.entity;

import com.example.chatservice.dto.RoomType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "chat_roomsv2")
@Getter
@NoArgsConstructor
public class ChatRoomV2 {

    @Id
    @Column(length = 36)
    private String roomId; // UUID

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoomType type;

    @Column(nullable = false)
    private int maxParticipants;

    @Column(nullable = false)
    private boolean largeRoom;

    @Column(nullable = false)
    private OffsetDateTime createdAt;

    public static ChatRoomV2 create(
            String name,
            RoomType type,
            int maxParticipants
    ) {
        ChatRoomV2 r = new ChatRoomV2();
        r.roomId = UUID.randomUUID().toString();
        r.name = name;
        r.type = type;
        r.maxParticipants = maxParticipants;
        r.largeRoom = maxParticipants >= 100;
        r.createdAt = OffsetDateTime.now();
        return r;
    }
}