package com.example.chatService.repository;

import com.example.chatService.entity.GroupOutbox;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GroupMessageOutboxRepository extends JpaRepository<GroupOutbox, Long> {

    List<GroupOutbox> findTop100ByProcessedFalseOrderByIdAsc();
}
