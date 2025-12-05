package com.example.chatservice.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "chat_room_participant")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ChatRoomParticipant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String roomId;
    private String username;

    public ChatRoomParticipant(String roomId, String username) {
        this.roomId = roomId;
        this.username = username;
    }
}
