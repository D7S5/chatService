package com.example.chatservice.repository;

import com.example.chatservice.entity.ChatRoomParticipant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatRoomParticipantRepository extends JpaRepository<ChatRoomParticipant, Long> {
    List<ChatRoomParticipant> findByUsername(String username);
}
