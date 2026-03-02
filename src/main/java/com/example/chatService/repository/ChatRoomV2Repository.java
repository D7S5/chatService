package com.example.chatService.repository;

import com.example.chatService.entity.ChatRoom;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChatRoomV2Repository extends JpaRepository<ChatRoom, String> {

    List<ChatRoom> findAll();

    boolean existsByRoomIdAndOwnerUserId(String roomId, String ownerId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from ChatRoom r where r.roomId = :roomId")
    ChatRoom findByIdForUpdate(@Param("roomId") String roomId);

    @Query("select r.roomId from ChatRoom r")
    List<String> findAllRoomIds();
}
