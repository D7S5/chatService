package com.example.chatservice.entity;

import com.example.chatservice.dto.RoomRole;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Entity
@Getter @Setter
public class Participant {

    @Id
    @GeneratedValue
    private Long id;
    private String roomId;
    private String userId;
    private RoomRole role;
    private boolean isActive;

    private OffsetDateTime joinedAt;
    private OffsetDateTime leftAt;

    public static Participant join(String roomId, String userId) {
        Participant p = new Participant();
        p.roomId = roomId;
        p.userId = userId;
        p.isActive = true;
        p.joinedAt = OffsetDateTime.now();
        return p;
    }

    public void leave() {
        this.isActive = false;
        this.leftAt = OffsetDateTime.now();
    }
}