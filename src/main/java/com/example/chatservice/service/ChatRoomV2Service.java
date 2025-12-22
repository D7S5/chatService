package com.example.chatservice.service;

import com.example.chatservice.dto.CreateRoomRequest;
import com.example.chatservice.dto.ParticipantDto;
import com.example.chatservice.dto.RoomResponse;
import com.example.chatservice.entity.ChatRoomV2;
import com.example.chatservice.repository.ChatRoomV2Repository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChatRoomV2Service {

    private final StringRedisTemplate redis;
    private final SimpMessagingTemplate messagingTemplate;
    private final ChatRoomV2Repository chatRoomV2Repository;

    private String usersKey(String roomId) {
        return "room:" + roomId + ":users";
    }

    private String countKey(String roomId) {
        return "room:" + roomId + ":count";
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
        return RoomResponse.from(room);
    }

    /* ======================
       입장
    ====================== */
    public void enter(String roomId, String userId, String username) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("username must not be null or blank");
        }

        redis.opsForHash().put(usersKey(roomId), userId, username);
        updateCount(roomId);
        broadcast(roomId);
    }

    /* ======================
       퇴장
    ====================== */
    public void leave(String roomId, String userId) {
        redis.opsForHash().delete(usersKey(roomId), userId);
        updateCount(roomId);
        broadcast(roomId);
    }

    /* ======================
       인원 수 갱신
    ====================== */
    private int updateCount(String roomId) {
        Long count = redis.opsForHash().size(usersKey(roomId));
        int current = count != null ? count.intValue() : 0;
        redis.opsForValue().set(countKey(roomId), String.valueOf(current));
        return current;
    }

    /* ======================
       공통 broadcast
    ====================== */
    private void broadcast(String roomId) {
        int current = Optional
                .ofNullable(redis.opsForValue().get(countKey(roomId)))
                .map(Integer::parseInt)
                .orElse(0);

        // 인원 수
        messagingTemplate.convertAndSend(
                "/topic/room-count/" + roomId,
                Map.of("current", current)
        );

        // 참여자 목록
        List<ParticipantDto> users =
                redis.opsForHash().entries(usersKey(roomId))
                        .entrySet()
                        .stream()
                        .map(e -> new ParticipantDto(
                                e.getKey().toString(),   // userId
                                e.getValue().toString() // username
                        ))
                        .toList();

        messagingTemplate.convertAndSend(
                "/topic/room-users/" + roomId,
                users
        );
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
