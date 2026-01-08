package com.example.chatservice.repository;

import com.example.chatservice.entity.ChatRoomV2;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChatRoomV2Repository extends JpaRepository<ChatRoomV2, String> {

    List<ChatRoomV2> findAll();

    boolean existsByRoomIdAndOwnerUserId(String roomId, String ownerId);

    @Lock(LockModeType.OPTIMISTIC)
    @Query("select r from ChatRoomV2 r where r.roomId = :roomId")
    ChatRoomV2 findByIdForUpdate(@Param("roomId") String roomId);

    @Query("select r.roomId from ChatRoomV2 r")
    List<String> findAllRoomIds();
}
