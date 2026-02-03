package com.example.chatService.repository;

import com.example.chatService.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, String> {
    boolean existsByName(String name);
}