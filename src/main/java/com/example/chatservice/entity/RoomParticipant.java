package com.example.chatservice.entity;

import com.example.chatservice.dto.RoomRole;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Getter
@Setter
@Table(name = "room_participants")
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

    /** 마지막 활동 시각 (heartbeat / 메시지) */
    private OffsetDateTime lastActiveAt;

    /** 현재 방 소속 여부 (reconnect-safe) */
    @Column(nullable = false)
    private boolean isActive;

    /** 방 나간 시각 */
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
        this.isActive = true;
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
