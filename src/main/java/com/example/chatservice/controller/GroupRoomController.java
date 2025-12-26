package com.example.chatservice.controller;

import com.example.chatservice.dto.ParticipantDto;
import com.example.chatservice.dto.RoomResponse;
import com.example.chatservice.dto.UserEnterDto;
import com.example.chatservice.entity.ChatRoomV2;
import com.example.chatservice.entity.GroupMessageEntity;
import com.example.chatservice.repository.ChatRoomV2Repository;
import com.example.chatservice.repository.GroupMessageRepository;
import com.example.chatservice.service.ChatRoomV2Service;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.security.Key;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/rooms")
public class GroupRoomController {

    private final ChatRoomV2Repository chatRoomV2Repository;
    private final ChatRoomV2Service roomV2Service;
    private final StringRedisTemplate redisTemplate;
    private final GroupMessageRepository groupMessageRepository;

    @GetMapping("/{roomId}")
    public ChatRoomV2 getRoom(@PathVariable String roomId) {
        return chatRoomV2Repository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room is not found"));
    }

    @GetMapping("/{roomId}/participants")
    public List<ParticipantDto> participants(@PathVariable String roomId) {
        return roomV2Service.getParticipants(roomId);
    }

    @GetMapping("/{roomId}/messages")
    public List<UserEnterDto.ChatMessageResponse> messages(
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
                .map(UserEnterDto.ChatMessageResponse::from)
                .toList();
    }

    @GetMapping("/with-count")
    public List<RoomResponse> getRoomsWithCount() {
        return chatRoomV2Repository.findAll().stream()
                .map(room -> {
                    String key = "room:" + room.getRoomId() + ":users";

                    int currentCount = Optional
                                    .ofNullable(redisTemplate.opsForHash()
                                            .size(key))
                                    .map(Long::intValue)
                                    .orElse(0);
                    return RoomResponse.from(room, currentCount);
                })
                .toList();
    }
}
