package com.example.chatservice.service;

import com.example.chatservice.dto.ParticipantDto;
import com.example.chatservice.dto.RoomRole;
import com.example.chatservice.entity.ChatRoomV2;
import com.example.chatservice.entity.RoomParticipant;
import com.example.chatservice.repository.ChatRoomV2Repository;
import com.example.chatservice.repository.RoomParticipantRepository;
import com.example.chatservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;

import java.time.Duration;
import java.util.List;

import static com.example.chatservice.dto.RoomRole.MEMBER;
import static com.example.chatservice.dto.RoomRole.OWNER;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class RoomParticipantServiceImpl implements RoomParticipantService {

    private final RoomParticipantRepository repository;
    private final UserRepository userRepository;
    private final StringRedisTemplate redis;

    private final ChatRoomV2Repository roomRepository;

    private final ParticipantEventPublisherImpl publisher;

    @Override
    @Transactional
    public void joinRoom(String roomId, String userId) {

        boolean ownerUser = checkOwnerUser(roomId, userId);

        if (ownerUser) {
            CheckOwner(roomId, userId, OWNER);
        } else {
            CheckOwner(roomId, userId, MEMBER);
        }
    }

    private void CheckOwner(String roomId, String userId, RoomRole roomRole) {
        RoomParticipant p = repository.findByRoomIdAndUserId(roomId, userId)
                .orElseGet(() -> repository.save(
                        RoomParticipant.builder()
                                .roomId(roomId)
                                .userId(userId)
                                .role(roomRole)
                                .build()
                ));

        p.activate();
        repository.save(p);
        syncRedisJoin(roomId, userId);

        publisher.broadcastJoin(roomId, toDto(p));
    }
    @Override
    public void reconnect(String roomId, String userId) {
//        joinRoom(roomId, userId); // 동일 로직
    }

    private RoomParticipant createNewParticipant(String roomId, String userId) {
        boolean ownerExists =
                repository.existsByRoomIdAndRoleAndIsActive(roomId, OWNER, true);

        RoomRole role = ownerExists ? MEMBER : OWNER;

        return RoomParticipant.builder()
                .roomId(roomId)
                .userId(userId)
                .role(role)
                .build();
    }

    /* =======================
       LEAVE
       ======================= */

    @Override
    public void leaveRoom(String roomId, String userId) {
        RoomParticipant participant = getParticipant(roomId, userId);

        participant.deactivate();
        repository.save(participant);

        syncRedisLeave(roomId, userId);

        publisher.broadcastLeave(
                roomId,
                toDto(participant)
        );

        // 방장 자동 위임
//        if (participant.getRole() == OWNER) {
//            autoTransferOwner(roomId);
//        }
    }

    /* =======================
       KICK / BAN
       ======================= */

    @Override
    public void kick(String roomId, String targetUserId, String byUserId) {
        requireAdmin(roomId, byUserId);

        if (byUserId.equals(targetUserId)) {
            throw new IllegalStateException("Cannot kick yourself");
        }
        RoomParticipant target = getParticipant(roomId, targetUserId);

        target.deactivate();

        repository.save(target);
        syncRedisLeave(roomId, targetUserId);

        publisher.broadcastLeave(
                roomId,
                toDto(target),
                "KICK"
        );
    }

    @Override
    public void ban(String roomId, String targetUserId, String byUserId, String reason) {
        requireOwner(roomId, byUserId);

        ChatRoomV2 room = roomRepository.findByRoomId(roomId)
                .orElseThrow();

        if (!room.getOwnerUserId().equals(byUserId)) {
            throw new SecurityException("Owner only");
        }

        if (targetUserId.equals(byUserId)) {
            throw new IllegalStateException("Cannot ban yourself");
        }

        RoomParticipant target = getParticipant(roomId, targetUserId);

        target.ban(reason);

        repository.save(target);
        syncRedisLeave(roomId, targetUserId);
    }

    /* =======================
       ROLE
       ======================= */

    @Override
    public void changeRole(
            String roomId,
            String targetUserId,
            RoomRole role,
            String byUserId
    ) {
        validateOwner(roomId, byUserId);

        RoomParticipant target = getParticipant(roomId, targetUserId);
        target.changeRole(role);

        repository.save(target);
    }

    @Override
    public void transferOwnership(
            String roomId,
            String newOwnerId,
            String byUserId
    ) {
        requireOwner(roomId, byUserId);

        ChatRoomV2 room = roomRepository.findByIdForUpdate(roomId);

        if (!room.getOwnerUserId().equals(byUserId)) {
            new SecurityException("Owner only");
        }

        if (byUserId.equals(newOwnerId)) return;

        RoomParticipant currentOwner =
                repository.findByRoomIdAndUserId(roomId, byUserId)
                                .orElseThrow();
        RoomParticipant newOwner =
                repository.findByRoomIdAndUserId(roomId, newOwnerId)
                                .orElseThrow();

        room.setOwnerUserId(newOwnerId);
        roomRepository.save(room);

        publisher.broadcastOwnerChanged(roomId, newOwnerId);
    }

    private void requireOwner(String roomId, String userId) {
        ChatRoomV2 room = roomRepository.findById(roomId).orElseThrow();

        if (!room.getOwnerUserId().equals(userId)) {
            throw new SecurityException("OWNER only");
        }
    }

    private void requireAdmin(String roomId, String userId) {
        ChatRoomV2 room = roomRepository.findById(roomId).orElseThrow();

        if (room.getOwnerUserId().equals(userId)) return;

        RoomParticipant p =
                repository.findByRoomIdAndUserId(roomId, userId)
                        .orElseThrow();

        if (p.getRole() != RoomRole.ADMIN) {
            throw new SecurityException("ADMIN only");
        }
    }
    public boolean checkOwnerUser(String roomId, String userId) {
        return roomRepository.existsByRoomIdAndOwnerUserId(roomId, userId);
    }

    /* =======================
       QUERY
       ======================= */

    @Override
    @Transactional(readOnly = true)
    public List<RoomParticipant> getActiveParticipants(String roomId) {
        return repository.findAllByRoomIdAndIsActiveTrue(roomId);
    }

    @Override
    public List<ParticipantDto> getParticipants(@PathVariable String roomId) {

        return getActiveParticipants(roomId)
                .stream()
                .map(p -> new ParticipantDto(
                        p.getUserId(),
                        loadUsername(p.getUserId()),
                        p.getRole()
                ))
                .toList();
    }

    /* =======================
       INTERNAL
       ======================= */


    private RoomParticipant getParticipant(String roomId, String userId) {
        return repository.findByRoomIdAndUserId(roomId, userId)
                .orElseThrow(() ->
                        new IllegalStateException("Participant not found"));
    }

//    private void validateAdmin(String roomId, String userId) {
//        RoomParticipant p = getParticipant(roomId, userId);
//        if (p.getRole() == MEMBER) {
//            throw new SecurityException("No permission");
//        }
//    }

    private void validateOwner(String roomId, String userId) {
        RoomParticipant p = getParticipant(roomId, userId);
        if (p.getRole() != OWNER) {
            throw new SecurityException("Owner only");
        }
    }

    private void autoTransferOwner(String roomId) {
        List<RoomParticipant> actives =
                repository.findAllByRoomIdAndIsActiveTrue(roomId);

        actives.stream()
                .filter(p -> p.getRole() == RoomRole.ADMIN)
                .findFirst()
                .or(() -> actives.stream().findFirst())
                .ifPresent(newOwner -> {
                    newOwner.changeRole(OWNER);
                    repository.save(newOwner);

                    publisher.broadcastOwnerChanged(
                            roomId,
                            newOwner.getUserId()
                    );
                });
    }

    /* =======================
       REDIS SYNC
       ======================= */

    private void syncRedisJoin(String roomId, String userId) {
        redis.opsForSet()
                .add("room:" + roomId + ":users", userId);
    }

    private void syncRedisLeave(String roomId, String userId) {
        redis.opsForSet()
                .remove("room:" + roomId + ":users", userId);
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

    private ParticipantDto toDto(RoomParticipant p) {
        return new ParticipantDto(
                p.getUserId(),
                loadUsername(p.getUserId()),
                p.getRole()
        );
    }
}
