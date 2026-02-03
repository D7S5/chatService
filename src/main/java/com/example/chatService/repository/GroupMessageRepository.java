package com.example.chatService.repository;

import com.example.chatService.entity.GroupMessageEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface GroupMessageRepository extends JpaRepository<GroupMessageEntity, Long> {

    @Query("""
        SELECT m FROM GroupMessageEntity m
        WHERE m.roomId = :roomId
        ORDER BY m.createdAt DESC
    """)
    List<GroupMessageEntity> findRecent(
            @Param("roomId") String roomId,
            Pageable pageable
    );
}
