package com.example.chatService.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@Data
@Entity
@Table(name = "chat_message")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String roomId;
    private String sender;
    private String recipient;
    private String content;
    @Enumerated(EnumType.STRING)
    private MessageType type;

    private OffsetDateTime timestamp;

    @PrePersist
    public void prePersist() {
        timestamp = OffsetDateTime.now();
    }

    public ChatMessage(String content, String sender, MessageType type) {
        this.content = content;
        this.sender = sender;
        this.type = type;
    }

    public enum MessageType {
        CHAT, JOIN, LEAVE, DM
    }
}
