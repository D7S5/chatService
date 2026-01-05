package com.example.chatservice.repository;

import com.example.chatservice.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, String> {
    boolean existsByName(String name);
}