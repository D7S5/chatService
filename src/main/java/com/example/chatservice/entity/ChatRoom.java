package com.example.chatservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "chat_room")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoom {

    @Id
    @Column(length = 36)
    private String roomId = UUID.randomUUID().toString();

    @Column(nullable = false, unique = true)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChatRoomType type; // "PUBLIC" or "PRIVATE"

    private String dmKey;

    public ChatRoom(String name) {
        this.roomId = UUID.randomUUID().toString();
        this.name = name;
    }
    public ChatRoom(String name, ChatRoomType type, String dmKey) {
        this.roomId = UUID.randomUUID().toString();
        this.name = name;
        this.type = type;
        this.dmKey = dmKey;
    }

    public enum ChatRoomType {
        PUBLIC, PRIVATE
    }
}