package com.example.chatService.repository;

import com.example.chatService.dto.MessagingStatus;
import com.example.chatService.entity.DMOutbox;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DMOutboxRepository extends JpaRepository<DMOutbox, Long> {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
        UPDATE dm_outbox
        SET status = 'PROCESSING',
            locked_by = :workerId,
            locked_at = NOW()
        WHERE status = 'NEW'
        ORDER BY id
        LIMIT :limit
        """, nativeQuery = true)
    int claimBatch(@Param("workerId") String workerId, @Param("limit") int limit);

    List<DMOutbox> findByStatusAndLockedByOrderByIdAsc(MessagingStatus status, String lockedBy);
}
