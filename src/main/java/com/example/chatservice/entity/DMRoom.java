package com.example.chatservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Builder
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "dm_rooms")
public class DMRoom {

    @Id
    @Column(length = 36)
    private String roomId;

    @Column(nullable = false, length = 36)
    private String userAId;
    @Column(nullable = false, length = 36)
    private String userBId;

    @Builder.Default
    private LocalDateTime lastMessageTime = LocalDateTime.now();
}
