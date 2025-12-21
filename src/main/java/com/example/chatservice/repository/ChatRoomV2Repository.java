package com.example.chatservice.repository;

import com.example.chatservice.entity.ChatRoomV2;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatRoomV2Repository extends JpaRepository<ChatRoomV2, String> {
}
