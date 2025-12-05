package com.example.chatservice.service;

import com.example.chatservice.entity.ChatRoom;
import com.example.chatservice.entity.Message;
import com.example.chatservice.entity.User;
import com.example.chatservice.model.ChatMessage;
import com.example.chatservice.repository.ChatMessageRepository;
import com.example.chatservice.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatMessageService {
    private final ChatMessageRepository chatMessageRepository;
    private final MessageRepository messageRepository;

    public List<ChatMessage> getMessages(String roomId) {
        return chatMessageRepository.findByRoomIdOrderByTimestampAsc(roomId);
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
