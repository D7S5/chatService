package com.example.chatService.repository;

import com.example.chatService.entity.Message;
import com.example.chatService.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, String> {
    List<Message> findBySenderAndReceiverOrderBySentAtAsc(User sender, User receiver);
    List<Message> findByReceiverAndIsReadFalse(User receiver);
}