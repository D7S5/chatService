package com.example.chatService.service;

import com.example.chatService.dto.CreateRoomRequest;
import com.example.chatService.dto.RoomResponse;
import com.example.chatService.dto.RoomRole;
import com.example.chatService.dto.RoomType;
import com.example.chatService.entity.ChatRoom;
import com.example.chatService.entity.RoomParticipant;
import com.example.chatService.repository.ChatRoomRepository;
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
    private final ChatRoomRepository chatRoomV2Repository;
    private final RoomParticipantRepository repository;
    private final RoomParticipantService service;
    private final RoomInviteService inviteService;

    public List<ChatRoom> getAllRooms() {
        return chatRoomV2Repository.findAll();
    }

    @Transactional
    public RoomResponse create(CreateRoomRequest req, String userId) {
        if (req.getMaxParticipants() < 2) {
            throw new IllegalArgumentException("최소 인원은 2명입니다.");
        }

        ChatRoom room = ChatRoom.create(
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
            inviteService.joinByInvite(inviteToken, userId);
        }

        RoomResponse res = RoomResponse.of(room, inviteToken);

        return res;
    }

    public RoomResponse getRoom(String roomId, String userId) {
        ChatRoom room = chatRoomV2Repository.findById(roomId)
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
        ChatRoom room = chatRoomV2Repository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found"));
        service.joinRoom(roomId, userId);
    }
}