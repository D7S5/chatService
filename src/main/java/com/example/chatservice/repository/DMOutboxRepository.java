package com.example.chatservice.repository;

import com.example.chatservice.entity.DMOutbox;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DMOutboxRepository extends JpaRepository<DMOutbox, Long> {

    List<DMOutbox> findTop100ByProcessedFalseOrderByIdAsc();

}
