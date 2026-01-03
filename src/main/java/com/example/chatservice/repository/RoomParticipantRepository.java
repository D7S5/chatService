package com.example.chatservice.repository;

import com.example.chatservice.dto.RoomRole;
import com.example.chatservice.entity.RoomParticipant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RoomParticipantRepository extends JpaRepository<RoomParticipant, Long> {
    List<RoomParticipant> findAllByRoomIdAndIsActiveTrue(String roomId);

    boolean existsByRoomIdAndRoleAndIsActive(
            String roomId,
            RoomRole role,
            boolean isActive
    );

    List<RoomParticipant> findAllByRoomIdAndRoleAndIsActiveTrue(
            String roomId,
            RoomRole role
    );

    boolean existsByRoomIdAndUserId(String roomId, String userId);

    Optional<RoomParticipant> findByRoomIdAndUserId(String roomId, String userId);

    List<RoomParticipant> findByRoomId(String roomId);

    long countByRoomId(String roomId);

    void deleteByRoomIdAndUserId(String roomId, String userId);

    boolean existsByOwnerRoomId(String roomId);
}
