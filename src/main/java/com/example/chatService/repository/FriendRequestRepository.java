package com.example.chatService.repository;

import com.example.chatService.entity.FriendRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FriendRequestRepository extends JpaRepository<FriendRequest, Long>
{
    @Query("SELECT fr FROM FriendRequest fr " +
            "JOIN FETCH fr.fromUser " +
            "JOIN FETCH fr.toUser " +
            "WHERE fr.toUser.id = :userId AND fr.status = 'PENDING'")
    List<FriendRequest> findReceivedRequests(@Param("userId") String userId);

}
