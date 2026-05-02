package com.example.chatService.service;

import com.example.chatService.dto.RoomRole;
import com.example.chatService.dto.RoomType;
import com.example.chatService.entity.ChatRoom;
import com.example.chatService.entity.RoomParticipant;
import com.example.chatService.event.ParticipantForcedExitEvent;
import com.example.chatService.event.RoomParticipantsChangedEvent;
import com.example.chatService.exception.BannedFromRoomException;
import com.example.chatService.repository.ChatRoomV2Repository;
import com.example.chatService.repository.RoomParticipantRepository;
import com.example.chatService.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.AccessDeniedException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RoomParticipantServiceImplTest {

    private final RoomParticipantRepository participantRepository = mock(RoomParticipantRepository.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final StringRedisTemplate redis = mock(StringRedisTemplate.class);
    private final ChatRoomV2Repository roomRepository = mock(ChatRoomV2Repository.class);
    private final ParticipantEventPublisherImpl participantEventPublisher = mock(ParticipantEventPublisherImpl.class);
    private final SimpMessagingTemplate messagingTemplate = mock(SimpMessagingTemplate.class);
    private final ApplicationEventPublisher eventPublisher = mock(ApplicationEventPublisher.class);

    private final RoomParticipantServiceImpl service = new RoomParticipantServiceImpl(
            participantRepository,
            userRepository,
            redis,
            roomRepository,
            participantEventPublisher,
            messagingTemplate,
            eventPublisher
    );

    @Test
    void joinRoomRejectsBannedUser() {
        when(participantRepository.existsByRoomIdAndUserIdAndIsBannedTrue("room-1", "user-1"))
                .thenReturn(true);

        assertThatThrownBy(() -> service.joinRoom("room-1", "user-1"))
                .isInstanceOf(BannedFromRoomException.class);

        verify(roomRepository, never()).findByIdForUpdate(any());
    }

    @Test
    void joinAsRoleCreatesAndActivatesParticipantAndIncrementsRoomCount() {
        ChatRoom room = room("owner-1", 10);
        when(participantRepository.findByRoomIdAndUserId("room-1", "user-1"))
                .thenReturn(Optional.empty());
        when(participantRepository.save(any(RoomParticipant.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(roomRepository.findByIdForUpdate("room-1")).thenReturn(room);

        service.joinAsRole("room-1", "user-1", RoomRole.MEMBER);

        assertThat(room.getCurrentCount()).isEqualTo(1);
        verify(participantRepository, times(2)).save(any(RoomParticipant.class));
        verify(eventPublisher).publishEvent(any(RoomParticipantsChangedEvent.class));
    }

    @Test
    void leaveRoomDoesNothingWhenParticipantAlreadyInactive() {
        RoomParticipant participant = participant("room-1", "user-1", RoomRole.MEMBER, false);
        when(participantRepository.findByRoomIdAndUserId("room-1", "user-1"))
                .thenReturn(Optional.of(participant));

        service.leaveRoom("room-1", "user-1");

        verify(roomRepository, never()).findByIdForUpdate(any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void kickRequiresAdminOrOwner() {
        RoomParticipant requester = participant("room-1", "member-1", RoomRole.MEMBER, true);
        when(participantRepository.findByRoomIdAndUserId("room-1", "member-1"))
                .thenReturn(Optional.of(requester));

        assertThatThrownBy(() -> service.kick("room-1", "target-1", "member-1"))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void kickDeactivatesTargetDecrementsCountAndPublishesEvents() {
        ChatRoom room = room("owner-1", 10);
        room.increaseCount();
        RoomParticipant admin = participant("room-1", "admin-1", RoomRole.ADMIN, true);
        RoomParticipant target = participant("room-1", "target-1", RoomRole.MEMBER, true);
        when(participantRepository.findByRoomIdAndUserId("room-1", "admin-1"))
                .thenReturn(Optional.of(admin));
        when(participantRepository.findByRoomIdAndUserId("room-1", "target-1"))
                .thenReturn(Optional.of(target));
        when(roomRepository.findByIdForUpdate("room-1")).thenReturn(room);

        service.kick("room-1", "target-1", "admin-1");

        assertThat(target.isActive()).isFalse();
        assertThat(room.getCurrentCount()).isZero();
        verify(eventPublisher).publishEvent(any(ParticipantForcedExitEvent.class));
        verify(eventPublisher).publishEvent(any(RoomParticipantsChangedEvent.class));
    }

    @Test
    void transferOwnershipRejectsNonOwner() {
        ChatRoom room = room("owner-1", 10);
        when(roomRepository.findById("room-1")).thenReturn(Optional.of(room));

        assertThatThrownBy(() -> service.transferOwnership("room-1", "new-owner", "other-user"))
                .isInstanceOf(SecurityException.class);

        verify(roomRepository, never()).findByIdForUpdate(any());
        verify(participantEventPublisher, never()).broadcastOwnerChanged(any(), any());
    }

    @Test
    void transferOwnershipUpdatesRoomAndBroadcastsWhenOwnerRequests() {
        ChatRoom room = room("owner-1", 10);
        when(roomRepository.findById("room-1")).thenReturn(Optional.of(room));
        when(roomRepository.findByIdForUpdate("room-1")).thenReturn(room);

        service.transferOwnership("room-1", "new-owner", "owner-1");

        assertThat(room.getOwnerUserId()).isEqualTo("new-owner");
        verify(roomRepository).save(room);
        verify(participantEventPublisher).broadcastOwnerChanged("room-1", "new-owner");
    }

    private ChatRoom room(String ownerUserId, int maxParticipants) {
        return ChatRoom.create("room", RoomType.PUBLIC, maxParticipants, ownerUserId);
    }

    private RoomParticipant participant(String roomId, String userId, RoomRole role, boolean active) {
        return RoomParticipant.builder()
                .roomId(roomId)
                .userId(userId)
                .role(role)
                .isActive(active)
                .build();
    }
}
