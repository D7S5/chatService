package com.example.chatService.controller;

import com.example.chatService.dto.ChatMessageResponse;
import com.example.chatService.dto.RoomResponse;
import com.example.chatService.entity.GroupMessageEntity;
import com.example.chatService.repository.ChatRoomV2Repository;
import com.example.chatService.repository.GroupMessageRepository;
import com.example.chatService.service.GroupRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/rooms")
public class GroupRoomController {

    private final ChatRoomV2Repository chatRoomV2Repository;
    private final GroupRoomService groupRoomService;

    @GetMapping("/{roomId}/messages")
    public List<ChatMessageResponse> messages(
            @PathVariable String roomId,
            @RequestParam(defaultValue = "50") int limit
    ) {
        return groupRoomService.messages(roomId, limit);
    }

    @GetMapping("/with-count")
    public List<RoomResponse> getRoomsWithCount() {
        return groupRoomService.getRoomsWithCount();
    }
}