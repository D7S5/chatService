package com.example.chatservice.controller;

import com.example.chatservice.dto.ChatMessageDto;
import com.example.chatservice.dto.CreateRoomRequest;
import com.example.chatservice.dto.RoomResponse;
import com.example.chatservice.entity.ChatRoom;
import com.example.chatservice.entity.ChatRoomV2;
import com.example.chatservice.service.ChatRoomV2Service;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.OffsetDateTime;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api")
public class ChatControllerV2 {

    private final SimpMessagingTemplate messaging;

    private final ChatRoomV2Service chatRoomV2Service;
    @MessageMapping("/chat.send")
    public void send(ChatMessageDto dto) {
        messaging.convertAndSend(
                "/topic/chat/" + dto.getRoomId(),
                new ChatMessageDto(
                        dto.getRoomId(),
                        dto.getSenderId(),
                        dto.getSenderName(),
                        dto.getContent(),
                        OffsetDateTime.now()
                )
        );
    }
    @PostMapping("/rooms")
    public RoomResponse create(@RequestBody CreateRoomRequest request) {
        return chatRoomV2Service.createV2(request);
    }
    @GetMapping("/rooms")
    public ResponseEntity<List<ChatRoomV2>> getChatRooms() {
        try {
            List<ChatRoomV2> rooms = chatRoomV2Service.getAllRooms();
            return ResponseEntity.ok(rooms);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
