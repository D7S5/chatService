package com.example.chatService.entity;

import com.example.chatService.dto.RoomType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "chat_rooms")
@Getter @Setter
@NoArgsConstructor
public class ChatRoom {

    @Id
    @Column(length = 36)
    private String roomId; // UUID

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoomType type;

    @Column(nullable = false)
    private int currentCount;

    @Column(nullable = false)
    private int maxParticipants;

    @Version
    private long version;

    @Column(nullable = false)
    private boolean largeRoom;

    @Column(nullable = false)
    private String ownerUserId;

    @Column(nullable = false)
    private OffsetDateTime createdAt;

    public static ChatRoom create(
            String name,
            RoomType type,
            int maxParticipants,
            String ownerUserId
    ) {
        ChatRoom r = new ChatRoom();
        r.roomId = UUID.randomUUID().toString();
        r.name = name;
        r.type = type;
        r.maxParticipants = maxParticipants;
        r.largeRoom = maxParticipants >= 100;
        r.ownerUserId = ownerUserId;
        r.createdAt = OffsetDateTime.now();
        return r;
    }

    public void increaseCount() {
        if (currentCount >= maxParticipants) {
            throw new IllegalStateException("Room is full");
        }
        this.currentCount++;
    };

    public void decreaseCount() {
//        if (currentCount <= 0) {
//            throw new IllegalStateException("Room count underflow");
//        }
        this.currentCount--;
    };
}