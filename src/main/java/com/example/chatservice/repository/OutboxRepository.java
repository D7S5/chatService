package com.example.chatservice.repository;

import com.example.chatservice.entity.DMMessageOutbox;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OutboxRepository extends JpaRepository<DMMessageOutbox, Long> {
}
