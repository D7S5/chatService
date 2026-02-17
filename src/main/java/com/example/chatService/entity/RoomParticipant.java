package com.example.chatService.entity;

import com.example.chatService.dto.RoomRole;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Getter
@Setter
@Table(
        name = "room_participant",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_room_owner",
                        columnNames = "owner_room_id"
                )
        }
)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomParticipant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "room_id", nullable = false, length = 36)
    private String roomId;

    @Column(name = "user_id", nullable = false, length = 36)
    private String userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RoomRole role;

    @Column(nullable = false)
    OffsetDateTime joinedAt;

    @Column(name = "owner_room_id", unique = true)
    private String ownerRoomId;

    private OffsetDateTime lastActiveAt;

    @Column(nullable = false)
    private boolean isActive;

    private OffsetDateTime leftAt;

    @Column(nullable = false)
    private boolean isBanned;

    private OffsetDateTime bannedAt;

    @Column(length = 255)
    private String banReason;

    @Column(nullable = false)
    private OffsetDateTime createdAt;

    @Column(nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        OffsetDateTime now = OffsetDateTime.now();
        this.joinedAt = now;
        this.createdAt = now;
        this.updatedAt = now;
        this.isActive = false;
        this.isBanned = false;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }

    public void activate() {
        this.isActive = true;
        this.leftAt = null;
        this.lastActiveAt = OffsetDateTime.now();
    }

    public void deactivate() {
        this.isActive = false;
        this.leftAt = OffsetDateTime.now();
    }

    public void ban(String reason) {
        this.isBanned = true;
        this.isActive = false;
        this.bannedAt = OffsetDateTime.now();
        this.banReason = reason;
    }

    public void changeRole(RoomRole role) {
        this.role = role;
    }
}
