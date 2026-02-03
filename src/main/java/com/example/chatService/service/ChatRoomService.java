package com.example.chatService.service;

import com.example.chatService.entity.ChatRoom;
import com.example.chatService.repository.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

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
        ChatRoom room = new ChatRoom(name, ChatRoom.ChatRoomType.PUBLIC);
        return chatRoomRepository.save(room);
    }


}