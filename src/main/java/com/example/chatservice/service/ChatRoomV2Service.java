package com.example.chatservice.service;

import com.example.chatservice.dto.CreateRoomRequest;
import com.example.chatservice.dto.ParticipantDto;
import com.example.chatservice.dto.RoomResponse;
import com.example.chatservice.entity.ChatRoom;
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
    /* 입장 */
    public void enter(String roomId, String userId, String username) {
        redis.opsForHash().put(usersKey(roomId), userId, username);

        Long count = redis.opsForHash().size(usersKey(roomId));
        redis.opsForValue().set(countKey(roomId), String.valueOf(count));

        broadcast(roomId);
    }

    /* 퇴장 */
    public void leave(String roomId, String userId) {
        redis.opsForHash().delete(usersKey(roomId), userId);

        Long count = redis.opsForHash().size(usersKey(roomId));
        redis.opsForValue().set(countKey(roomId), String.valueOf(count));

        broadcast(roomId);
    }

    /* 공통 broadcast */
    private void broadcast(String roomId) {
        int current = Optional
                .of(redis.opsForValue().get(countKey(roomId)))
                .map(Integer::parseInt)
                .orElse(0);

        // 인원 수
        messagingTemplate.convertAndSend(
                "/topic/room-count/" + roomId,
                Map.of("current", current)
                        );

        // 참여자
        List<ParticipantDto> users =
                redis.opsForHash().entries(usersKey(roomId))
                        .entrySet().stream()
                        .map(e -> new ParticipantDto(
                                e.getKey().toString(),
                                e.getValue().toString()
                        ))

                        .toList();

        messagingTemplate.convertAndSend(
                "/topic/room-users/" + roomId,
                users
        );
    }

    public List<ParticipantDto> getParticipants(String roomId) {
        return redis.opsForHash().entries(usersKey(roomId))
                .entrySet().stream()
                .map(e -> new ParticipantDto(
                        e.getKey().toString(),
                        e.getValue().toString()
                ))
                .toList();
    }
}
