package com.example.chatservice.repository;

import com.example.chatservice.entity.MessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KafkaMessageRepository extends JpaRepository<MessageEntity, Long> {
}
