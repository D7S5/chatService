package com.example.chatService.service;

import com.example.chatService.dto.AdminChangedResponse;
import com.example.chatService.dto.ParticipantDto;
import com.example.chatService.dto.RoomCountDto;
import com.example.chatService.dto.RoomRole;
import com.example.chatService.entity.ChatRoomV2;
import com.example.chatService.entity.RoomParticipant;
import com.example.chatService.event.ParticipantForcedExitEvent;
import com.example.chatService.event.RoomParticipantsChangedEvent;
import com.example.chatService.exception.BannedFromRoomException;
import com.example.chatService.repository.ChatRoomV2Repository;
import com.example.chatService.repository.RoomParticipantRepository;
import com.example.chatService.repository.UserRepository;
import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.retry.annotation.Retryable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

import static com.example.chatService.dto.RoomRole.*;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class
RoomParticipantServiceImpl implements RoomParticipantService {

    private final RoomParticipantRepository repository;
    private final UserRepository userRepository;
    private final StringRedisTemplate redis;
    private final ChatRoomV2Repository roomRepository;
    private final ParticipantEventPublisherImpl publisher;
    private final SimpMessagingTemplate messagingTemplate;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Retryable(
            value = OptimisticLockException.class,
            maxAttempts = 3
    )
    @Transactional
    public void joinRoom(String roomId, String userId) {

        if (repository.existsByRoomIdAndUserIdAndIsBannedTrue(roomId, userId)) {
            throw new BannedFromRoomException(roomId);
        }
        // 유저 권한 확인
        RoomRole role = hasPermission(roomId, userId);

        joinAsRole(roomId, userId, role);
//        System.out.println(role);
    }

    public RoomRole hasPermission(String roomId, String userId) {
        Optional<RoomParticipant> member =
                repository.findByRoomIdAndUserId(roomId, userId);

        if (member.isEmpty()) return MEMBER;

        return member.get().getRole();
    }
    @Transactional
    public void joinAsRole(String roomId, String userId, RoomRole roomRole) {

        RoomParticipant p = repository.findByRoomIdAndUserId(roomId, userId)
                .orElseGet(() -> repository.save(
                        RoomParticipant.builder()
                                .roomId(roomId)
                                .userId(userId)
                                .role(roomRole)
                                .build()
                ));

//        System.out.println("isActive = " + p.isActive());

        if (p.isActive()) {
            return;
        }

        ChatRoomV2 room = roomRepository.findByIdForUpdate(roomId);
//        System.out.println("[BEFORE] room currentCount = " + room.getCurrentCount());
        room.increaseCount();
//        System.out.println("[AFTER] room currentCount = " + room.getCurrentCount());

        p.activate();
        repository.save(p);

        eventPublisher.publishEvent(
                new RoomParticipantsChangedEvent(roomId)
        );
    }

    @Override
    @Transactional
    public void leaveRoom(String roomId, String userId) {

        ChatRoomV2 room = roomRepository.findByIdForUpdate(roomId);
        room.decreaseCount();

        RoomParticipant participant = getParticipant(roomId, userId);

        participant.deactivate();
        repository.save(participant);

        eventPublisher.publishEvent(
                new RoomParticipantsChangedEvent(roomId)
        );
    }

    @Override
    public void reconnect(String roomId, String userId) {
    }

    @Override
    @Transactional
    public void kick(String roomId, String targetUserId, String byUserId) {
        requireAdmin(roomId, byUserId);

        if (byUserId.equals(targetUserId)) {
            throw new IllegalStateException("Cannot kick yourself");
        }

        RoomParticipant target = getParticipant(roomId, targetUserId);

        if (target.getRole() == RoomRole.OWNER) {
            throw new IllegalStateException("OWNER는 강퇴할 수 없습니다.");
        }

        target.deactivate();

        repository.save(target);

        ChatRoomV2 room = roomRepository.findByIdForUpdate(roomId);
        room.decreaseCount();

        eventPublisher.publishEvent(
                new ParticipantForcedExitEvent(
                        roomId,
                        targetUserId,
                        "KICK"
                )
        );

        eventPublisher.publishEvent(
                new RoomParticipantsChangedEvent(roomId)
        );
    }

    @Override
    @Transactional
    public void ban(String roomId, String targetUserId, String byUserId, String reason) {
        requireOwner(roomId, byUserId);

        if (targetUserId.equals(byUserId)) {
            throw new IllegalStateException("Cannot ban yourself");
        }

        RoomParticipant target = repository
                .findByRoomIdAndUserId(roomId, targetUserId)
                .orElseThrow(() -> new IllegalStateException("Target not in room"));

        if (target.getRole() == RoomRole.OWNER) {
            throw new IllegalStateException("Cannot ban OWNER");
        }

        target.ban(reason);   // isBanned=true, isActive=false

        ChatRoomV2 room = roomRepository.findByIdForUpdate(roomId);
        room.decreaseCount();

        repository.save(target);

        eventPublisher.publishEvent(
                new ParticipantForcedExitEvent(
                        roomId,
                        targetUserId,
                        reason
                )
        );
        eventPublisher.publishEvent(
                new RoomParticipantsChangedEvent(roomId)
        );
    }

    @Override
    @Transactional
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
    @Transactional
    public AdminChangedResponse toggleAdmin(
            String roomId,
            String requesterId,
            String targetUserId
    ) {
        RoomParticipant requester = repository
                .findByRoomIdAndUserId(roomId, requesterId)
                .orElseThrow();

        if (requester.getRole() != RoomRole.OWNER)
            throw new AccessDeniedException("OWNER만 가능");

        RoomParticipant target = repository
                .findByRoomIdAndUserId(roomId, targetUserId)
                .orElseThrow();

        if (target.getRole() == RoomRole.OWNER)
            throw new IllegalStateException("OWNER는 변경 불가");

        target.setRole(
                target.getRole() == ADMIN ? MEMBER : ADMIN
        );

        repository.save(target);

        return new AdminChangedResponse(
                targetUserId,
                target.getRole().name()
        );
    }


    @Override
    @Transactional
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
        RoomParticipant p = repository
                .findByRoomIdAndUserId(roomId, userId)
                .orElseThrow(() -> new AccessDeniedException("Not in room"));

        if (p.getRole() == RoomRole.MEMBER) {
            throw new AccessDeniedException("ADMIN only");
        }
    }
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

    private RoomParticipant getParticipant(String roomId, String userId) {
        return repository.findByRoomIdAndUserId(roomId, userId)
                .orElseThrow(() ->
                        new IllegalStateException("Participant not found"));
    }

    private void validateOwner(String roomId, String userId) {
        RoomParticipant p = getParticipant(roomId, userId);
        if (p.getRole() != OWNER) {
            throw new SecurityException("Owner only");
        }
    }
    @Override
    @Transactional
    public int getCurrentCount(String roomId) {
        return repository.countByRoomIdAndIsActiveTrue(roomId);
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

    @Override
    @Transactional
    public void broadcast(String roomId) {
        int current = getCurrentCount(roomId);

        ChatRoomV2 room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("broadcast"));

        messagingTemplate.convertAndSend(
                "/topic/room-users/" + roomId,
                "UPDATED"
        );

        RoomCountDto dto = new RoomCountDto(
                current,
                room.getMaxParticipants()
        );
        messagingTemplate.convertAndSend(
                "/topic/rooms/" + roomId + "/count",
                dto
        );
    }
    public boolean isParticipant(String roomId, String userId) {
        return repository.existsByRoomIdAndUserId(roomId, userId);
    }
}
