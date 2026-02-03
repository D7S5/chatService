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

    /** 마지막 활동 시각 (heartbeat / 메시지) */
    private OffsetDateTime lastActiveAt;

    @Column(nullable = false)
    private boolean isActive;

    private OffsetDateTime leftAt;

    /** 밴 여부 */
    @Column(nullable = false)
    private boolean isBanned;

    /** 밴 시각 */
    private OffsetDateTime bannedAt;

    /** 밴 사유 */
    @Column(length = 255)
    private String banReason;

    /** 생성 시각 */
    @Column(nullable = false)
    private OffsetDateTime createdAt;

    /** 수정 시각 */
    @Column(nullable = false)
    private OffsetDateTime updatedAt;

    /* =======================
       Lifecycle
       ======================= */

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

    /* =======================
       Domain Methods
       ======================= */


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
