package com.example.chatService.repository;

import com.example.chatService.entity.DMRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DMRoomRepository extends JpaRepository<DMRoom, String> {
    Optional<DMRoom> findByUserAIdAndUserBId(String a, String b);
    Optional<DMRoom> findByUserBIdAndUserAId(String a, String b);
    @Query("SELECT r FROM DMRoom r WHERE r.userAId = :userId OR r.userBId = :userId ORDER BY r.lastMessageTime DESC")
    List<DMRoom> findByUser(@Param("userId") String userId);

}
