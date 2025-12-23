package com.example.chatservice.repository;

import com.example.chatservice.entity.GroupOutbox;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GroupMessageOutboxRepository extends JpaRepository<GroupOutbox, Long> {

    List<GroupOutbox> findTop100ByProcessedFalseOrderByIdAsc();
}
