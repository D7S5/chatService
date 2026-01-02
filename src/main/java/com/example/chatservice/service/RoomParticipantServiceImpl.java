package com.example.chatservice.service;

import com.example.chatservice.dto.RoomRole;
import com.example.chatservice.entity.RoomParticipant;
import com.example.chatservice.repository.RoomParticipantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class RoomParticipantServiceImpl implements RoomParticipantService {

    private final RoomParticipantRepository repository;
    private final StringRedisTemplate redisTemplate;

    /* =======================
       JOIN / RECONNECT
       ======================= */

    @Override
    public void joinRoom(String roomId, String userId) {
        RoomParticipant participant = repository
                .findByRoomIdAndUserId(roomId, userId)
                .orElseGet(() -> createNewParticipant(roomId, userId));

        if (participant.isBanned()) {
            throw new IllegalStateException("Banned user");
        }

        participant.activate();
        repository.save(participant);

        syncRedisJoin(roomId, userId);
    }

    @Override
    public void reconnect(String roomId, String userId) {
        joinRoom(roomId, userId); // 동일 로직
    }

    private RoomParticipant createNewParticipant(String roomId, String userId) {
        boolean ownerExists =
                repository.existsByRoomIdAndRoleAndIsActive(roomId, RoomRole.OWNER, true);

        RoomRole role = ownerExists ? RoomRole.MEMBER : RoomRole.OWNER;

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

        // 방장 자동 위임
        if (participant.getRole() == RoomRole.OWNER) {
            autoTransferOwner(roomId);
        }
    }

    /* =======================
       KICK / BAN
       ======================= */

    @Override
    public void kick(String roomId, String targetUserId, String byUserId) {
        validateAdmin(roomId, byUserId);

        RoomParticipant target = getParticipant(roomId, targetUserId);
        target.deactivate();

        repository.save(target);
        syncRedisLeave(roomId, targetUserId);
    }

    @Override
    public void ban(String roomId, String targetUserId, String byUserId, String reason) {
        validateAdmin(roomId, byUserId);

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
        validateOwner(roomId, byUserId);

        RoomParticipant currentOwner = getParticipant(roomId, byUserId);
        RoomParticipant newOwner = getParticipant(roomId, newOwnerId);

        currentOwner.changeRole(RoomRole.ADMIN);
        newOwner.changeRole(RoomRole.OWNER);

        repository.save(currentOwner);
        repository.save(newOwner);
    }

    /* =======================
       QUERY
       ======================= */

    @Override
    @Transactional(readOnly = true)
    public List<RoomParticipant> getActiveParticipants(String roomId) {
        return repository.findAllByRoomIdAndIsActiveTrue(roomId);
    }

    /* =======================
       INTERNAL
       ======================= */

    private RoomParticipant getParticipant(String roomId, String userId) {
        return repository.findByRoomIdAndUserId(roomId, userId)
                .orElseThrow(() ->
                        new IllegalStateException("Participant not found"));
    }

    private void validateAdmin(String roomId, String userId) {
        RoomParticipant p = getParticipant(roomId, userId);
        if (p.getRole() == RoomRole.MEMBER) {
            throw new SecurityException("No permission");
        }
    }

    private void validateOwner(String roomId, String userId) {
        RoomParticipant p = getParticipant(roomId, userId);
        if (p.getRole() != RoomRole.OWNER) {
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
                    newOwner.changeRole(RoomRole.OWNER);
                    repository.save(newOwner);
                });
    }

    /* =======================
       REDIS SYNC
       ======================= */

    private void syncRedisJoin(String roomId, String userId) {
        redisTemplate.opsForSet()
                .add("room:" + roomId + ":users", userId);
    }

    private void syncRedisLeave(String roomId, String userId) {
        redisTemplate.opsForSet()
                .remove("room:" + roomId + ":users", userId);
    }
}
