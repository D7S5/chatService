package com.example.chatservice.repository;

import com.example.chatservice.entity.Message;
import com.example.chatservice.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, String> {
    List<Message> findBySenderAndReceiverOrderBySentAtAsc(User sender, User receiver);
    List<Message> findByReceiverAndIsReadFalse(User receiver);
}