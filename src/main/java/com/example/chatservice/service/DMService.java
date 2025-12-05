
package com.example.chatservice.service;

import com.example.chatservice.dto.DMRoomDto;
import com.example.chatservice.entity.DMMessage;
import com.example.chatservice.entity.DMRoom;
import com.example.chatservice.entity.User;
import com.example.chatservice.repository.DMMessageRepository;
import com.example.chatservice.repository.DMRoomRepository;
import com.example.chatservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class DMService {

    private final DMRoomRepository roomRepository;
    private final DMMessageRepository dmMessageRepository;
    private final UserRepository userRepository;

    public DMRoom startOrGetRoom(String userA, String userB) {
        return roomRepository.findByUserAIdAndUserBId(userA, userB)
                .or(() -> roomRepository.findByUserBIdAndUserAId(userA, userB))
                .orElseGet(() -> {
                    DMRoom room = DMRoom.builder()
                            .roomId(UUID.randomUUID().toString())
                            .userAId(userA)
                            .userBId(userB)
                            .build();
                    return roomRepository.save(room);
                });
    }

    public List<DMMessage> getMessages(String roomId) {
        DMRoom room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));
        return dmMessageRepository.findByRoomOrderBySentAtAsc(room);
    }

    public DMMessage sendMessage(String roomId, String senderId, String content) {
        DMRoom room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        DMMessage msg = DMMessage.builder()
                .room(room)
                .senderId(senderId)
                .content(content)
                .sentAt(LocalDateTime.now())
                .isRead(false)
                .build();

        room.setLastMessageTime(LocalDateTime.now());
        roomRepository.save(room);

        return dmMessageRepository.save(msg);
    }

    public String getReceiverId(String roomId, String senderId) {
        DMRoom room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("방 없음"));

        return room.getUserAId().equals(senderId)
                ? room.getUserBId()
                : room.getUserAId();
    }
    public List<DMRoomDto> getUserRoomsWithUnread(String myUserId) {
        List<DMRoom> rooms = roomRepository.findByUser(myUserId);

        return rooms
                .stream()
                .map(room -> {
                    String targetUserId = room.getUserAId().equals(myUserId) ? room.getUserBId() : room.getUserAId();

            User targetUser = userRepository.findById(targetUserId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            int unread = dmMessageRepository.countUnread(room.getRoomId(), myUserId);

            return new DMRoomDto(
                    room.getRoomId(),
                    targetUser.getId(),
                    targetUser.getUsername(), // 여기서 내 닉네임이 아니라 상대 닉네임 가져오기
                    unread
            );
        }).toList();
    }

    @Transactional
    public void markMessagesAsRead(String roomId, String userId) {
        dmMessageRepository.markAsReadByRoomAndReceiver(roomId, userId);
    }
    public int countUnreadMessages(String roomId, String userId) {
        return dmMessageRepository.countUnread(roomId, userId);
    }
}