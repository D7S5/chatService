package com.example.chatService.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "dm_outbox")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DMOutbox {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String roomId;
    private String senderId;

    @Column(columnDefinition = "TEXT")
    private String content;

    private long eventTimestamp;

    private boolean processed;

    private OffsetDateTime createAt;

}
