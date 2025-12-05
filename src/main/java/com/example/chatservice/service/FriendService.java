package com.example.chatservice.service;

import com.example.chatservice.component.WebSocketEventPublisher;
import com.example.chatservice.dto.*;
import com.example.chatservice.entity.Friend;
import com.example.chatservice.entity.FriendRequest;
import com.example.chatservice.entity.User;
import com.example.chatservice.repository.FriendRepository;
import com.example.chatservice.repository.FriendRequestRepository;
import com.example.chatservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static java.util.stream.Collectors.toList;

@Service
@RequiredArgsConstructor
@Transactional
public class FriendService {

    private final UserRepository userRepository;
    private final FriendRepository friendRepository;
    private final FriendRequestRepository friendRequestRepository;
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

        // 실시간 알림
        eventPublisher.publishFriendEvent(toUserId,
                Map.of(
                        "type", "FRIEND_REQUEST",
                        "fromUserId", fromUserId,
                        "fromUserNickname", from.getUsername()
                )
        );

        return FriendRequestResponseDto.builder()
                .fromUserId(fromUserId)
                .toUserId(toUserId)
                .status(f.getStatus().name())
                .build();
    }

//    @Transactional(readOnly = true)
//    public List<Friend> getReceivedRequests(String userId) {
//        return friendRepository.findPendingRequestsReceivedByUserId(userId);
//    }

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
    /** 친구 요청 수락 */
//    @Transactional
//    public String accept(Long requestId) {
//
//        Friend req = friendRepository.findById(requestId)
//                .orElseThrow(() -> new IllegalArgumentException("요청 없음"));
//
//        if (req.getStatus() != Friend.FriendStatus.PENDING)
//            throw new IllegalStateException("이미 처리됨");
//
//        req.setStatus(Friend.FriendStatus.ACCEPTED);
//
//        // 실시간 알림: 요청 보낸 사람에게 (user)
//        eventPublisher.publishFriendEvent(
//                req.getUser().getId(),
//                Map.of(
//                        "type", "FRIEND_ACCEPTED",
//                        "friendId", req.getFriend().getId()
//                )
//        );
//
//        // 실시간 알림: 요청 받은 사람에게 (friend)
//        eventPublisher.publishFriendEvent(
//                req.getFriend().getId(),
//                Map.of(
//                        "type", "FRIEND_ACCEPTED",
//                        "friendId", req.getUser().getId()
//                )
//        );
//
//        return "수락 완료";
//    }

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

        eventPublisher.publishFriendEvent(
                userA.getId(),
                Map.of(
                        "type", "Friend_ACCEPTED",
                        "friendId", userB.getId()
                )
        );
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
