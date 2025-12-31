package com.example.chatservice.service;

import com.example.chatservice.dto.CreateRoomRequest;
import com.example.chatservice.dto.ParticipantDto;
import com.example.chatservice.dto.RoomEnterDto;
import com.example.chatservice.dto.RoomResponse;
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

    public RoomResponse createV2(CreateRoomRequest req) {
        if (req.getMaxParticipants() < 2) {
            throw new IllegalArgumentException("최소 인원은 2명입니다.");
        }

        ChatRoomV2 room = ChatRoomV2.create(
                req.getName(),
                req.getType(),
                req.getMaxParticipants()
        );
        chatRoomV2Repository.save(room);
        RoomResponse res = RoomResponse.create(room);

        return res;
    }


    private String usersKey(String roomId) {
        return "room:" + roomId + ":sessions";
    }
    public void enter(RoomEnterDto dto, SimpMessageHeaderAccessor accessor) {

        String sessionId = accessor.getSessionId();
        String roomId = dto.getRoomId();
        String userId = dto.getUserId();
        String username = dto.getUsername();

        String existingRoom =
                redis.opsForValue().get("RoomSession:" + sessionId + ":room");

        if (existingRoom != null && !existingRoom.equals(roomId)) {
            leaveBySession(existingRoom, sessionId);
        }

        redis.opsForValue().set(
                "RoomSession:" + sessionId + ":room",
                roomId
        );
        redis.opsForValue().set(
                "RoomSession:" + sessionId + ":user",
                userId
        );
        redis.opsForHash().put(
                "room:" + roomId + ":sessions",
                sessionId,
                userId
        );

//      username 캐싱
        if (username != null && !username.isBlank()) {
            redis.opsForValue().set(
                    "user:" + userId + ":username",
                    username,
                    Duration.ofHours(1)
            );
        }
        broadcastRoomCount(roomId);
        broadcastGetCurrentCount(roomId);
    }

    private ChatRoomV2 getRoomOrThrow(String roomId) {
        return chatRoomV2Repository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room is not found"));
    }
    public void leave(SimpMessageHeaderAccessor accessor) {

        String sessionId = accessor.getSessionId();

        String roomId =
                redis.opsForValue().get("RoomSession:" + sessionId + ":room");
        String userId =
                redis.opsForValue().get("RoomSession:" + sessionId + ":user");

        if (roomId == null || userId == null) return;

        redis.opsForHash().delete(
                "room:" + roomId + ":sessions",
                sessionId
        );

        redis.delete("RoomSession:" + sessionId + ":room");
        redis.delete("RoomSession:" + sessionId + ":user");

        log.info("LEAVE room={}, user={}, session={}", roomId, userId, sessionId);

        broadcastRoomCount(roomId);
        broadcastGetCurrentCount(roomId);
    }

    public void leaveBySession(String roomId, String sessionId) {

        redis.opsForHash().delete(
                "room:" + roomId + ":sessions",
                sessionId
        );

        redis.delete("RoomSession:" + sessionId + ":room");
        redis.delete("RoomSession:" + sessionId + ":user");

        broadcastRoomCount(roomId);
        broadcastGetCurrentCount(roomId);
    }

    private void broadcastRoomCount(String roomId) {
        int current = getCurrentCount(roomId);

        // 인원 수
        messagingTemplate.convertAndSend(
                "/topic/room-count/" + roomId,
                Map.of("current", current)
        );
    }

    private void broadcastGetCurrentCount(String roomId) {
        List<ParticipantDto> users = getParticipants(roomId);

        messagingTemplate.convertAndSend(
                "/topic/room-users/" + roomId,
                users
        );
    }

    private int getCurrentCount(String roomId) {
        Map<Object, Object> sessions = redis.opsForHash().entries(usersKey(roomId));
        return (int) sessions.values().stream()
                .distinct()
                .count();
    }

    /* REST 조회용 */
//    public List<ParticipantDto> getParticipants(String roomId) {
//        return redis.opsForHash().entries(usersKey(roomId))
//                .entrySet()
//                .stream()
//                .filter(e -> e.getValue() != null)
//                .map(e -> new ParticipantDto(
//                        e.getValue().toString(),
//                        loadUsername(e.getValue().toString())
//                ))
//                .toList();
//    }

    public List<ParticipantDto> getParticipants(String roomId) {
        Map<Object, Object> sessions =
                redis.opsForHash().entries(usersKey(roomId));

        return sessions.values().stream()
                .filter(Objects::nonNull)
                .distinct()
                .map(userId -> new ParticipantDto(
                        userId.toString(),
                        loadUsername(userId.toString())
                )).toList();
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