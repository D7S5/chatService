package com.example.chatService.entity;

import com.example.chatService.dto.MessagingStatus;
import com.example.chatService.dto.ChatMessageType;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "group_outbox")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupOutbox {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String roomId;
    private String senderId;
    private String senderName;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "message_type", nullable = false, length = 10,
            columnDefinition = "varchar(10) default 'TEXT'")
    @Builder.Default
    private ChatMessageType messageType = ChatMessageType.TEXT;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    private long eventTimestamp;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MessagingStatus status; // NEW , PROCESSING, SENT

    @Column(name = "locked_by", length = 64)
    private String lockedBy;

    @Column(name = "locked_at")
    private OffsetDateTime lockedAt;

    private OffsetDateTime createAt;

}
