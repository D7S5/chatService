package com.example.chatService.entity;

import com.example.chatService.dto.MessagingStatus;
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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MessagingStatus status; // NEW/PROCESSING/SENT

    @Column(name = "locked_by", length = 64)
    private String lockedBy;

    @Column(name = "locked_at")
    private OffsetDateTime lockedAt;

    private OffsetDateTime createAt;

}
