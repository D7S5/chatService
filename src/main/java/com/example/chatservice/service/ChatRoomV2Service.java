package com.example.chatservice.service;

import com.example.chatservice.dto.*;
import com.example.chatservice.entity.ChatRoomV2;
import com.example.chatservice.repository.ChatRoomV2Repository;
import com.example.chatservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChatRoomV2Service {

    private final StringRedisTemplate redis;
    private final SimpMessagingTemplate messagingTemplate;
    private final ChatRoomV2Repository chatRoomV2Repository;

    private final UserRepository userRepository;

    public List<ChatRoomV2> getAllRooms() {
        return chatRoomV2Repository.findAll();
    }

    public RoomResponse createV2(CreateRoomRequest req, String userId) {
        if (req.getMaxParticipants() < 2) {
            throw new IllegalArgumentException("최소 인원은 2명입니다.");
        }

        ChatRoomV2 room = ChatRoomV2.create(
                req.getName(),
                req.getType(),
                req.getMaxParticipants(),
                userId
        );
        room.setCurrentCount(room.getCurrentCount() + 1);
        chatRoomV2Repository.save(room);

        String inviteToken = null;
        if (req.getType() == RoomType.PRIVATE) {
            inviteToken = UUID.randomUUID().toString();
            redis.opsForValue().set(
                    "room:invite:" + inviteToken,
                    room.getRoomId(),
                    Duration.ofMinutes(10) // 초대만료
            );
        }
        RoomResponse res = RoomResponse.of(room, inviteToken);

        return res;
    }


    private String usersKey(String roomId) {
        return "room:" + roomId + ":sessions";
    }

    public void enter(RoomEnterDto dto, SimpMessageHeaderAccessor accessor) {

        String sessionId = accessor.getSessionId();
        String roomId = dto.getRoomId();
        String userId = (String) accessor.getSessionAttributes().get("userId");

//        System.out.println("Enter userId = " + userId + " roomId = " + roomId); // debug

        String existingRoom =
                redis.opsForValue().get("session:" + sessionId + ":room");

        if (existingRoom != null && !existingRoom.equals(roomId)) {
            leaveBySession(existingRoom, sessionId);
        }
        redis.opsForValue().set(
                "session:" + sessionId + ":room",
                roomId
        );
        redis.opsForValue().set(
                "session:" + sessionId + ":user",
                userId
        );
        redis.opsForHash().put(
                "room:" + roomId + ":sessions",
                sessionId,
                userId
        );

        broadcastRoomCount(roomId);
//        broadcastGetCurrentCount(roomId);
    }

    public void leaveBySession(String roomId, String sessionId) {

//        Map<Object, Object> list = redis.opsForHash().entries("room:"+ roomId + ":sessions");
//        System.out.println("Session list RoomId = " + roomId + ", list = " + list);

        redis.opsForHash().delete(
                "room:" + roomId + ":sessions",
                sessionId
        );

        redis.delete("session:" + sessionId + ":room");
        redis.delete("session:" + sessionId + ":user");

        broadcastRoomCount(roomId);
//        broadcastGetCurrentCount(roomId);
    }

    private void broadcastRoomCount(String roomId) {
        int current = getCurrentCount(roomId);

        // 인원 수
        messagingTemplate.convertAndSend(
                "/topic/room-count/" + roomId,
                Map.of("current", current)
        );
    }

//    private void broadcastGetCurrentCount(String roomId) {
//        List<ParticipantDto> users = getParticipants(roomId);
//
//        messagingTemplate.convertAndSend(
//                "/topic/room-users/" + roomId,
//                users
//        );
//    }

    private int getCurrentCount(String roomId) {
        Map<Object, Object> sessions = redis.opsForHash().entries(usersKey(roomId));
        return (int) sessions.values().stream()
                .distinct()
                .count();
    }

    private String loadUsername(String userId) {

        if (userId == null) return "UNKNOWN";

        String key = "user:" + userId + ":username";
        String cached = redis.opsForValue().get(key);

        if (cached != null) return cached;

        String fromDb = userRepository.findUsernameById(userId);

        if (fromDb == null) {
            log.warn("Username not found for userId={}", userId);
            return "UNKNOWN";
        }
        redis.opsForValue().set(key, fromDb, Duration.ofHours(1));
        return fromDb;
    }
}