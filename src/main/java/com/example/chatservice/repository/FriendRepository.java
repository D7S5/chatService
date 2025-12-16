package com.example.chatservice.repository;

import com.example.chatservice.dto.FriendStatus;
import com.example.chatservice.entity.Friend;
import com.example.chatservice.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FriendRepository extends JpaRepository<Friend, Long> {

    boolean existsByUserAndFriend(User user, User friend);
    List<Friend> findByFriend_Id(String friendId);

    @Query("""
            SELECT f FROM Friend f
            WHERE (f.user = :me AND f.friend = :target)
               OR (f.user = :target AND f.friend = :me)
            """)
    List<Friend> findFriendRelation(
            @Param("me") User me,
            @Param("target") User target
    );
    // 또는 더 안전하게 @Query 사용 (추천!)
    @Query("SELECT f FROM Friend f JOIN FETCH f.user u WHERE f.friend.id = :userId AND f.status = 'PENDING'")
    List<Friend> findPendingRequestsReceivedByUserId(@Param("userId") String userId);

    // 보낸 요청용
    @Query("SELECT f FROM Friend f JOIN FETCH f.friend u WHERE f.user.id = :userId AND f.status = 'PENDING'")
    List<Friend> findPendingRequestsSentByUserId(@Param("userId") String userId);

    @Query("SELECT f FROM Friend f " +
            "LEFT JOIN FETCH f.user " +
            "LEFT JOIN FETCH f.friend " +
            "WHERE f.user.id = :userId OR f.friend.id = :userId")
    List<Friend> findAllRelatedWithFetch(@Param("userId") String userId);
    List<Friend> findByUserId(String userId);

    @Query("SELECT f FROM Friend f JOIN FETCH f.user WHERE f.friend.id = :userId AND f.status = 'PENDING'")
    List<Friend> findReceivedRequests(@Param("userId") String userId);

    List<Friend> findByFriendId(String friendId);

    // 내가 보낸 + 내가 받은 친구 관계 모두 가져오기 (이게 진짜 정답!)
    @Query("SELECT f FROM Friend f WHERE f.user.id = :userId OR f.friend.id = :userId")
    List<Friend> findByUserIdOrFriendId(@Param("userId") String userId);

    // 상태까지 필터링하고 싶을 때 (더 자주 씀!)
    @Query("SELECT f FROM Friend f WHERE (f.user.id = :userId OR f.friend.id = :userId) AND f.status = :status")
    List<Friend> findByUserIdOrFriendIdAndStatus(
            @Param("userId") String userId,
            @Param("status") FriendStatus status);

    @Query("SELECT f FROM Friend f JOIN FETCH f.user JOIN FETCH f.friend WHERE f.user = :user OR f.friend = :user")
    List<Friend> findAllRelatedFriends(@Param("user") User user);

    @Query("SELECT f FROM Friend f JOIN FETCH f.user JOIN FETCH f.friend WHERE f.user = :user")
    List<Friend> findByUser(@Param("user") User user);

    @Query("SELECT f FROM Friend f JOIN FETCH f.user JOIN FETCH f.friend WHERE f.friend = :user")
    List<Friend> findByFriend(@Param("user") User user);


    boolean existsByUserIdAndFriendId(String userId, String targetUserId);

    // 양방향 관계 체크용
    boolean existsByUserIdAndFriendIdOrUserIdAndFriendId(
            String userId1, String friendId1,
            String userId2, String friendId2
    );
}