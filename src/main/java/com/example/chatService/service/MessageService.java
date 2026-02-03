package com.example.chatService.service;

import com.example.chatService.entity.ChatRoom;
import com.example.chatService.entity.Message;
import com.example.chatService.entity.User;
import com.example.chatService.repository.MessageRepository;
import com.example.chatService.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageService {
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;


    public Message sendMessage(String senderId, String receiverId, String content) {
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new RuntimeException("Sender not found"));
        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new RuntimeException("Receiver not found"));
        Message message = new Message();
        message.setSender(sender);
        message.setReceiver(receiver);
        message.setContent(content);

        return messageRepository.save(message);
    }

    public List<Message> getChatHistoryById(String userIdA, String userIdB) {
        User userA = userRepository.findById(userIdA)
                .orElseThrow(() -> new RuntimeException("UserA not found"));
        User userB = userRepository.findById(userIdB)
                .orElseThrow(() -> new RuntimeException("UserB not found"));

        List<Message> messages1 = messageRepository.findBySenderAndReceiverOrderBySentAtAsc(userA, userB);
        List<Message> messages2 = messageRepository.findBySenderAndReceiverOrderBySentAtAsc(userB, userA);

        messages1.addAll(messages2);
        messages1.sort((m1, m2) -> m1.getSentAt().compareTo(m2.getSentAt()));

        return messages1;
    }

    public Message saveMessage(User sender, User receiver, ChatRoom room, String content) {
        Message msg = new Message();
        msg.setSender(sender);
        msg.setReceiver(receiver);
        msg.setRoom(room);
        msg.setContent(content);
        msg.setRead(false);
        return messageRepository.save(msg);
    }
}