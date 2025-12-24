package com.example.chatservice.repository;

import com.example.chatservice.entity.ChatRoomV2;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChatRoomV2Repository extends JpaRepository<ChatRoomV2, String> {

    List<ChatRoomV2> findAll();

    Optional<ChatRoomV2> findByRoomId(String roomId);
}
