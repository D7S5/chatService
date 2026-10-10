package com.example.chatService.service;

import com.example.chatService.component.WebSocketEventPublisher;
import com.example.chatService.dto.FriendStatus;
import com.example.chatService.entity.DMRoom;
import com.example.chatService.entity.Friend;
import com.example.chatService.entity.User;
import com.example.chatService.repository.DMMessageRepository;
import com.example.chatService.repository.DMOutboxRepository;
import com.example.chatService.repository.DMRoomRepository;
import com.example.chatService.repository.FriendRepository;
import com.example.chatService.repository.UserRepository;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FriendServiceTest {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final FriendRepository friendRepository = mock(FriendRepository.class);
    private final WebSocketEventPublisher eventPublisher = mock(WebSocketEventPublisher.class);
    private final DMRoomRepository dmRoomRepository = mock(DMRoomRepository.class);
    private final DMMessageRepository dmMessageRepository = mock(DMMessageRepository.class);
    private final DMOutboxRepository dmOutboxRepository = mock(DMOutboxRepository.class);

    private final FriendService friendService = new FriendService(
            userRepository,
            friendRepository,
            eventPublisher,
            dmRoomRepository,
            dmMessageRepository,
            dmOutboxRepository
    );

    @Test
    void removingFriendDeletesDmMessagesOutboxAndRoom() {
        User me = User.builder().id("user-1").build();
        User target = User.builder().id("user-2").build();
        DMRoom room = DMRoom.builder()
                .roomId("room-1")
                .userAId(me.getId())
                .userBId(target.getId())
                .build();
        when(userRepository.findById(me.getId())).thenReturn(Optional.of(me));
        when(userRepository.findById(target.getId())).thenReturn(Optional.of(target));
        when(dmRoomRepository.findAllBetweenUsers(me.getId(), target.getId())).thenReturn(List.of(room));

        friendService.removeFriendAndDMRoom(me.getId(), target.getId());

        var deletionOrder = inOrder(dmMessageRepository, dmOutboxRepository, dmRoomRepository, friendRepository);
        deletionOrder.verify(dmMessageRepository).deleteAllByRoomIds(List.of("room-1"));
        deletionOrder.verify(dmOutboxRepository).deleteAllByRoomIds(List.of("room-1"));
        deletionOrder.verify(dmRoomRepository).deleteAll(List.of(room));
        deletionOrder.verify(friendRepository).deleteFriendRelation(me.getId(), target.getId());
    }

    @Test
    void removingFriendWithoutDmRoomStillDeletesFriendRelation() {
        User me = User.builder().id("user-1").build();
        User target = User.builder().id("user-2").build();

        when(userRepository.findById(me.getId())).thenReturn(Optional.of(me));
        when(userRepository.findById(target.getId())).thenReturn(Optional.of(target));
        when(dmRoomRepository.findAllBetweenUsers(me.getId(), target.getId())).thenReturn(List.of());
        friendService.removeFriendAndDMRoom(me.getId(), target.getId());

        verify(dmMessageRepository, never()).deleteAllByRoomIds(List.of());
        verify(friendRepository).deleteFriendRelation(me.getId(), target.getId());
    }

    @Test
    void acceptingRequestUpdatesExistingRelationWithoutCreatingDuplicates() {
        User sender = User.builder().id("user-1").build();
        User receiver = User.builder().id("user-2").build();
        Friend request = Friend.builder()
                .id(1L)
                .user(sender)
                .friend(receiver)
                .status(FriendStatus.PENDING)
                .build();
        when(friendRepository.findById(1L)).thenReturn(Optional.of(request));

        friendService.acceptFriendRequest(1L);

        verify(friendRepository, times(1)).save(request);
        verify(eventPublisher).publishAcceptFriendEvent(eq(sender.getId()), any());
    }
}
