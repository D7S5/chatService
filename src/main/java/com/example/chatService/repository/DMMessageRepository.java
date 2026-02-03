package com.example.chatService.repository;

import com.example.chatService.entity.DMMessage;
import com.example.chatService.entity.DMRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DMMessageRepository extends JpaRepository<DMMessage, Long> {
    List<DMMessage> findByRoomOrderBySentAtAsc(DMRoom room);

    @Query("SELECT COUNT(m) FROM DMMessage m WHERE m.room.roomId = :roomId AND m.senderId <> :userId AND m.isRead = false")
    int countUnread(@Param("roomId") String roomId, @Param("userId") String userId);

    @Modifying
    @Query("update DMMessage m set m.isRead = true where m.room.roomId = :roomId and m.senderId <> :userId and m.isRead = false")
    void markAsReadByRoomAndReceiver(@Param("roomId") String roomId, @Param("userId") String userId);
}
