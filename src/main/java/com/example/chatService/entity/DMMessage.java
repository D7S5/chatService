package com.example.chatService.entity;

import com.example.chatService.dto.ChatMessageType;
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

    @Column(name = "sender_name", nullable = false)
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

    @Builder.Default
    private OffsetDateTime sentAt = OffsetDateTime.now();

    @Column(name = "is_read")
    @Builder.Default
    private boolean isRead = false;
}
