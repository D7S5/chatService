package com.example.chatService.service;

import com.example.chatService.component.WebSocketEventPublisher;
import com.example.chatService.dto.*;
import com.example.chatService.entity.Friend;
import com.example.chatService.entity.User;
import com.example.chatService.repository.FriendRepository;
import com.example.chatService.repository.FriendRequestRepository;
import com.example.chatService.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class FriendService {

    private final UserRepository userRepository;
    private final FriendRepository friendRepository;
    private final WebSocketEventPublisher eventPublisher;

    /** 친구 요청 보내기 */
    @Transactional
    public FriendRequestResponseDto sendFriendRequest(String fromUserId, String toUserId) {
        User from = userRepository.findById(fromUserId)
                .orElseThrow(() -> new IllegalArgumentException("From user not found"));

        User to = userRepository.findById(toUserId)
                .orElseThrow(() -> new IllegalArgumentException("Target user not found"));

        if (from.getId().equals(to.getId())) {
            throw new IllegalArgumentException("자기 자신에게 보낼 수 없음");
        }

        if (friendRepository.existsByUserAndFriend(from, to) || friendRepository.existsByUserAndFriend(to, from)) {
            throw new IllegalArgumentException("이미 요청을 보냈거나 친구입니다.");
        }

        Friend f = Friend.builder()
                .user(from)
                .friend(to)
                .status(FriendStatus.PENDING)
                .build();

        friendRepository.save(f);

        PublishFriendEvent publishEvent = PublishFriendEvent.builder()
                                                .type("FRIEND_REQUEST")
                                                .fromUserId(fromUserId)
                                                .fromUserNickname(from.getUsername()).build();

        eventPublisher.publishFriendEvent(toUserId, publishEvent);

        return FriendRequestResponseDto.builder()
                .fromUserId(fromUserId)
                .toUserId(toUserId)
                .status(f.getStatus().name())
                .build();
    }

    @Transactional
    public List<FriendRequestDto> getReceivedRequests(String userId) {
        List<Friend> requests = friendRepository.findPendingRequestsReceivedByUserId(userId);

        return requests.stream()
                .map(friends -> FriendRequestDto.builder()
                        .id(friends.getId())
                        .toUserId(friends.getFriend().getId())
                        .fromUserNickname(friends.getUser().getUsername())
                        .fromUserId(friends.getUser().getId())
                        .status(friends.getStatus())
                        .build())
                .toList();
    }
    // 보낸 친구 요청 (PENDING)
    @Transactional(readOnly = true)
    public List<Friend> getSentRequests(String userId) {
        return friendRepository.findPendingRequestsSentByUserId(userId);
    }

    // 친구 목록 (ACCEPTED 상태만)
    @Transactional(readOnly = true)
    public List<User> getFriendList(String userId) {
        return friendRepository.findAllRelatedWithFetch(userId)
                .stream()
                .filter(f -> f.getStatus() == FriendStatus.ACCEPTED)
                .map(f -> f.getUser().getId().equals(userId) ? f.getFriend() : f.getUser())
                .distinct()
                .toList();
    }

    @Transactional
    public void removeFriend(String me, String target) {

        User userA = userRepository.findById(me)
                .orElseThrow(() -> new IllegalArgumentException("user not found, me = "  + me + " target id = " + target));
        User userB = userRepository.findById(target)
                .orElseThrow(() -> new IllegalArgumentException("user not found, me = "  + me + " target id = " + target));

        List<Friend> friends = friendRepository.findFriendRelation(userA, userB);

        friends.forEach(friendRepository::delete);

    }
    @Transactional
    public String acceptFriendRequest(Long requestId) {
        Friend request = friendRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("요청을 찾을 수 없습니다"));

        if ( request.getStatus() != FriendStatus.PENDING) {
            throw new IllegalStateException("이미 처리된 요청입니다");
        }

        User userA = request.getFriend(); // 요청 보낸사람
        User userB = request.getUser(); // 요청 받은사람

        request.setStatus(FriendStatus.ACCEPTED);
        friendRepository.save(request);

        Friend friendA = Friend.builder()
                .user(userA)
                .friend(userB)
                .status(FriendStatus.ACCEPTED)
                .build();

        Friend friendB = Friend.builder()
                .user(userB)
                .friend(userA)
                .status(FriendStatus.ACCEPTED)
                .build();

        friendRepository.save(friendA);
        friendRepository.save(friendB);

        PublishAcceptFriendEvent publishAcceptFriendEvent = PublishAcceptFriendEvent.builder()
                                                                .type("Friend_ACCEPTED")
                                                                .friendId(userB.getId()).build();

        eventPublisher.publishAcceptFriendEvent(
                            userA.getId(),
                            publishAcceptFriendEvent);

        return "친구 신청 수락 완료";
    }
    /** 친구 요청 거절 */
    @Transactional
    public String reject(Long requestId) {
        Friend req = friendRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("요청 없음"));

        if (req.getStatus() != FriendStatus.PENDING)
            throw new IllegalStateException("이미 처리됨");

        req.setStatus(FriendStatus.BLOCKED);
        return "거절 완료";
    }

    public boolean isAlreadyFriend(String userId, String targetUserId) {
        return friendRepository.existsByUserIdAndFriendIdOrUserIdAndFriendId(
                userId, targetUserId,
                targetUserId, userId
        );
    }
}
