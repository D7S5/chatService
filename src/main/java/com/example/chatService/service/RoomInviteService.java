package com.example.chatService.service;

import com.example.chatService.dto.JoinByInviteResponse;
import com.example.chatService.dto.RoomRole;
import com.example.chatService.dto.RoomType;
import com.example.chatService.entity.ChatRoomV2;
import com.example.chatService.repository.ChatRoomV2Repository;
import com.example.chatService.repository.RoomParticipantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.nio.file.AccessDeniedException;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class RoomInviteService {

    private final StringRedisTemplate redis;
    private final ChatRoomV2Repository roomRepository;
    private final RoomParticipantService roomParticipantService;
    private final RoomParticipantRepository participantRepository;

    private static final Duration INVITE_TTL = Duration.ofMinutes(10);

    public JoinByInviteResponse joinByInvite(String inviteCode, String userId) {

        String key = "room:invite:" + inviteCode;
        String roomId = redis.opsForValue().get(key);

        if (roomId == null) {
            throw new IllegalArgumentException("초대 코드가 유효하지 않습니다.");
        }

        ChatRoomV2 room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("방이 존재하지 않습니다."));

        if (room.getType() != RoomType.PRIVATE) {
            throw new IllegalStateException("비밀방이 아닙니다");
        }

        roomParticipantService.joinRoom(room.getRoomId(), userId);
        return new JoinByInviteResponse(room.getRoomId());
    }

    public String generateInviteCode(String roomId, String userId) throws AccessDeniedException {

        if (!participantRepository.existsByRoomIdAndUserIdAndRoleIn(
                roomId,
                userId,
                List.of(RoomRole.OWNER, RoomRole.ADMIN)
        )) {
            throw new AccessDeniedException("권한 없음");
        }

        ChatRoomV2 room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalStateException("방 없음"));

        if (room.getType() != RoomType.PRIVATE) {
            throw new IllegalStateException("비밀방 아님");
        }

        String inviteCode = UUID.randomUUID().toString();

        redis.opsForValue().set(
                "room:invite:" + inviteCode,
                roomId,
                INVITE_TTL
        );

        return inviteCode;
    }

    public String enterByInvite(String token) {

        String roomId = redis.opsForValue().get("room:invite:" + token);
        if (roomId == null) {
            throw new ResponseStatusException(HttpStatus.GONE, "INVITE_EXPIRED");
        }
        return roomId;
    }
}
