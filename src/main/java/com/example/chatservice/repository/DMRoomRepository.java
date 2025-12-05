package com.example.chatservice.repository;

import com.example.chatservice.entity.DMRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DMRoomRepository extends JpaRepository<DMRoom, String> {
    Optional<DMRoom> findByUserAIdAndUserBId(String a, String b);
    Optional<DMRoom> findByUserBIdAndUserAId(String a, String b);
    List<DMRoom> findByUserAIdOrUserBId(String uid1, String uid2);
    @Query("SELECT r FROM DMRoom r WHERE r.userAId = :userId OR r.userBId = :userId ORDER BY r.lastMessageTime DESC")
    List<DMRoom> findByUser(@Param("userId") String userId);

}
