package com.example.chatService.repository;

import com.example.chatService.entity.PrivateMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PrivateMessageRepository extends JpaRepository<PrivateMessage, Long> {
    List<PrivateMessage> findBySenderAndReceiverOrderBySentAtAsc(String sender, String receiver);
    List<PrivateMessage> findByReceiverAndSenderOrderBySentAtAsc(String receiver, String sender);

}
