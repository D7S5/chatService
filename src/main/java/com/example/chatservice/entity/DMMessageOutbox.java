package com.example.chatservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "dm_outbox")
@Getter @Setter @Builder
@AllArgsConstructor @NoArgsConstructor
public class DMMessageOutbox {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String roomId;
    private String senderId;

    @Column(columnDefinition = "TEXT")
    private String content;

    private OffsetDateTime sentAt;

    @Builder.Default
    private boolean processed = false;
}
