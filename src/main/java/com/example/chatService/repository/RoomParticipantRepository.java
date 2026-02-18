package com.example.chatService.repository;

import com.example.chatService.dto.RoomRole;
import com.example.chatService.entity.RoomParticipant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RoomParticipantRepository extends JpaRepository<RoomParticipant, Long> {
    List<RoomParticipant> findAllByRoomIdAndIsActiveTrue(String roomId);


    boolean existsByRoomIdAndUserIdAndRoleIn(String roomId, String userId, List<RoomRole> roles);

    boolean existsByRoomIdAndUserId(String roomId, String userId);

    Optional<RoomParticipant> findByRoomIdAndUserId(String roomId, String userId);


    boolean existsByRoomIdAndUserIdAndRole(String roomId, String userId, RoomRole role);


    boolean existsByRoomIdAndUserIdAndIsBannedTrue(String roomId, String userId);

    int countByRoomIdAndIsActiveTrue(String roomId);
}
