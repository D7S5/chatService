package com.example.chatservice.service;

import com.example.chatservice.entity.ChatRoom;
import com.example.chatservice.entity.ChatRoomParticipant;
import com.example.chatservice.repository.ChatRoomParticipantRepository;
import com.example.chatservice.repository.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;


    public List<ChatRoom> getAllRooms() {
        return chatRoomRepository.findAll();
    }

    public ChatRoom createPublicRoom(String name) {
        if (chatRoomRepository.existsByName(name)) {
            throw new IllegalArgumentException("이미 존재하는 채팅방 이름입니다.");
        }
        ChatRoom room = new ChatRoom(name, ChatRoom.ChatRoomType.PUBLIC, null);
        return chatRoomRepository.save(room);
    }


}