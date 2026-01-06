package com.example.chatservice.repository;

import com.example.chatservice.entity.RoomBan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoomBanRepository extends JpaRepository<RoomBan, Long> {
    boolean existsByRoomIdAndUserId(String roomId, String userId);
}
