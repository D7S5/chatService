package com.example.chatService.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Entity
@Table(
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"roomId", "userId"})
        }
)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomBan {

    @Id
    @GeneratedValue
    private Long id;

    private String roomId;
    private String userId;

    private String bannedBy; // OWNER
    private OffsetDateTime bannedAt;
}
