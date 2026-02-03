package com.example.chatService.entity;

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

    private long eventTimestamp;

    private boolean processed;

    private OffsetDateTime createAt;

}
