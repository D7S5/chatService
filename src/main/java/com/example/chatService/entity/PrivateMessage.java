package com.example.chatService.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@Entity
@Table(name = "private_message")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PrivateMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String sender;    // username (또는 이메일, 프로젝트 규약에 맞게)

    @Column(nullable = false)
    private String receiver;  // username

    @Column(nullable = false, length = 2000)
    private String content;

    @Column(nullable = false)
    private OffsetDateTime sentAt;

    private boolean readFlag = false;
}
