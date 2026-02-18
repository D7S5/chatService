package com.example.chatService.service;

import com.example.chatService.dto.*;
import com.example.chatService.entity.ChatRoomV2;
import com.example.chatService.entity.RoomParticipant;
import com.example.chatService.repository.ChatRoomV2Repository;
import com.example.chatService.repository.RoomParticipantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChatRoomService {

    private final StringRedisTemplate redis;
    private final ChatRoomV2Repository chatRoomV2Repository;
    private final RoomParticipantRepository repository;
    private final RoomParticipantService service;

    public List<ChatRoomV2> getAllRooms() {
        return chatRoomV2Repository.findAll();
    }

    @Transactional
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

        chatRoomV2Repository.save(room);

        repository.save(
                RoomParticipant.builder()
                        .roomId(room.getRoomId())
                        .userId(userId)
                        .role(RoomRole.OWNER)
                        .isActive(true)
                        .build()
        );

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

    public RoomResponse getRoom(String roomId, String userId) {
        ChatRoomV2 room = chatRoomV2Repository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found"));

        if (room.getType() == RoomType.PRIVATE) {
            boolean joined = service.isParticipant(roomId, userId);
            if (!joined) {
                return RoomResponse.inaccessible(room, "PRIVATE_ROOM");
            }
        }
        return RoomResponse.from(room);
    }

    public void joinRoom(String roomId, String userId) {
        ChatRoomV2 room = chatRoomV2Repository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found"));
        service.joinRoom(roomId, userId);
    }
}