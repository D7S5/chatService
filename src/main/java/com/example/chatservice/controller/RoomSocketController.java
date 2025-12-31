package com.example.chatservice.controller;

import com.example.chatservice.dto.ParticipantDto;
import com.example.chatservice.dto.RoomEnterDto;
import com.example.chatservice.repository.UserRepository;
import com.example.chatservice.service.ChatRoomV2Service;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class RoomSocketController {

    private final ChatRoomV2Service roomV2Service;

    private final StringRedisTemplate redis;
    private final SimpMessagingTemplate messagingTemplate;

    private final UserRepository userRepository;

    @MessageMapping("/room.enter")
    public void enter(RoomEnterDto dto, SimpMessageHeaderAccessor accessor) {

        roomV2Service.enter(dto, accessor);
        System.out.println("enter dto roomId = " + dto.getRoomId() + "dto userId " + dto.getUserId() + "dto username = " + dto.getUsername());
//        broadcastRoom(dto.getRoomId());
    }

    @MessageMapping("/room.leave")
    public void leave(RoomEnterDto dto, SimpMessageHeaderAccessor accessor) {
        roomV2Service.leave(accessor);

        System.out.println("leave dto roomId = " + dto.getRoomId() + "dto userId " + dto.getUserId() + "dto username = " + dto.getUsername());
//        broadcastRoom(dto.getRoomId());
    }


    // ========================================================================
    private void broadcastRoom(String roomId) {
        Map<Object, Object> sessions =
                redis.opsForHash().entries("room:" + roomId + ":sessions");

        int currentCount = (int) sessions.values().stream()
                .distinct()
                .count();

        messagingTemplate.convertAndSend(
                "/topic/room-count/" + roomId,
                Map.of("current", currentCount)
        );

//        System.out.println("current = " + currentCount);

        List<ParticipantDto> participants =
                sessions.values().stream()
                        .distinct()
                        .map(userId -> new ParticipantDto(
                                userId.toString(),
                                loadUsername(userId.toString())
                        )).toList();

//        System.out.println("participants = " + participants);

        messagingTemplate.convertAndSend(
                "/topic/room-users/" + roomId,
                participants
        );
    }

    private String loadUsername(String userId) {
        String key = "user:" + userId + ":username";
        String username = redis.opsForValue().get(key);

        if (username != null) return username;

        String fromDb = userRepository.findUsernameById(userId);
        redis.opsForValue().set(key, fromDb, Duration.ofHours(1));
        return fromDb;
    }
}