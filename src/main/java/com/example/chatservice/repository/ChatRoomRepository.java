package com.example.chatservice.repository;

import com.example.chatservice.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, String> {
    Optional<ChatRoom> findByRoomId(String roomId);
    Optional<ChatRoom> findByName(String name);
    Optional<ChatRoom> findByDmKey(String dmKey);
    boolean existsByName(String name);

    @Query("SELECT c FROM ChatRoom c WHERE c.type = 'PRIVATE' AND (c.dmKey LIKE %:username% )")
    List<ChatRoom> findAllDMs(@Param("username") String username);
}