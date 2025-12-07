package com.example.chatservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "dm_messages")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DMMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", referencedColumnName = "roomId")
    private DMRoom room;

    @Column(length = 36, nullable = false)
    private String senderId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;
    @Builder.Default
    private OffsetDateTime sentAt = OffsetDateTime.now();

    @Column(name = "is_read")
    @Builder.Default
    private boolean isRead = false;
}
