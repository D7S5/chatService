package com.example.chatService.controller;

import com.example.chatService.dto.ChatMessageResponse;
import com.example.chatService.dto.RoomResponse;
import com.example.chatService.entity.GroupMessageEntity;
import com.example.chatService.repository.ChatRoomV2Repository;
import com.example.chatService.repository.GroupMessageRepository;
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
    private final GroupMessageRepository groupMessageRepository;

//    @GetMapping("/{roomId}")
//    public ChatRoomV2 getRoom(@PathVariable String roomId) {
//        return chatRoomV2Repository.findById(roomId)
//                .orElseThrow(() -> new IllegalArgumentException("Room is not found"));
//    }

    @GetMapping("/{roomId}/messages")
    public List<ChatMessageResponse> messages(
            @PathVariable String roomId,
            @RequestParam(defaultValue = "50") int limit
    ) {
        int size = Math.min(limit, 200); // abuse 방지

        List<GroupMessageEntity> entities =
                groupMessageRepository.findRecent(
                        roomId,
                        PageRequest.of(0, size)
                );

        Collections.reverse(entities);

        return entities.stream()
                .map(ChatMessageResponse::from)
                .toList();
    }

    @GetMapping("/with-count")
    public List<RoomResponse> getRoomsWithCount() {
        List<RoomResponse> res = chatRoomV2Repository.findAll().stream()
                .map(room ->
                        RoomResponse.from(room)
                )
                .toList();

        return res;
    }
}