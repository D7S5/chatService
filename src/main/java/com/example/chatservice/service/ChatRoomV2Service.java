package com.example.chatservice.service;

import com.example.chatservice.dto.CreateRoomRequest;
import com.example.chatservice.dto.ParticipantDto;
import com.example.chatservice.dto.RoomEnterDto;
import com.example.chatservice.dto.RoomResponse;
import com.example.chatservice.entity.ChatRoomV2;
import com.example.chatservice.redis.OnlineStatusService;
import com.example.chatservice.redis.OnlineStatusServiceV2;
import com.example.chatservice.repository.ChatRoomV2Repository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChatRoomV2Service {

    private final StringRedisTemplate redis;
    private final SimpMessagingTemplate messagingTemplate;
    private final ChatRoomV2Repository chatRoomV2Repository;

//    private final OnlineStatusService onlineStatusService;

    private String usersKey(String roomId) {
        return "room:" + roomId + ":users";
    }

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

        System.out.println(res);

        return res;
    }

    public void enter(RoomEnterDto dto, SimpMessageHeaderAccessor accessor) {

        String sessionId = accessor.getSessionId();
        String roomId = dto.getRoomId();
        String userId = dto.getUserId();
        String username = dto.getUsername();

        String existingRoom =
                redis.opsForValue().get("RoomSession:" + sessionId + ":room");

        if (existingRoom != null) {
            log.debug("already entered room={}, session={}", existingRoom, sessionId);
            return;
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
        redis.opsForValue().set(
                "user:" + userId + ":username",
                username,
                Duration.ofHours(1)
        );

        log.info("ENTER room={}, user={}, session={}", roomId, userId, sessionId);
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
        Long count = redis.opsForHash().size(usersKey(roomId));
        return count != null ? count.intValue() : 0;
    }

    /* REST 조회용 */
    public List<ParticipantDto> getParticipants(String roomId) {
        return redis.opsForHash().entries(usersKey(roomId))
                .entrySet()
                .stream()
                .map(e -> new ParticipantDto(
                        e.getKey().toString(),
                        e.getValue().toString()
                ))
                .toList();
    }
}
