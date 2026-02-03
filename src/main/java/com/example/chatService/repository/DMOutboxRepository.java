package com.example.chatService.repository;

import com.example.chatService.entity.DMOutbox;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DMOutboxRepository extends JpaRepository<DMOutbox, Long> {

    List<DMOutbox> findTop100ByProcessedFalseOrderByIdAsc();

}
