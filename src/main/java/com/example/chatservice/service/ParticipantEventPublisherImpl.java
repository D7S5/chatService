package com.example.chatservice.service;

import com.example.chatservice.dto.*;
import com.example.chatservice.entity.ChatRoomV2;
import com.example.chatservice.repository.ChatRoomV2Repository;
import com.example.chatservice.repository.RoomParticipantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class ParticipantEventPublisherImpl implements ParticipantEventPublisher {

    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public void broadcastJoin(
            String roomId,
            ParticipantDto dto) {
        messagingTemplate.convertAndSend(
                "/topic/rooms/" + roomId + "/participants",
                new ParticipantEvent(
                        ParticipantEventType.JOIN,
                        roomId,
                        dto,
                        null
                )
        );
    }

    @Override
    public void broadcastLeave(
            String roomId,
            ParticipantDto dto
    ) {
        messagingTemplate.convertAndSend(
                "/topic/rooms/" + roomId + "/participants",
                new ParticipantEvent(
                        ParticipantEventType.LEAVE,
                        roomId,
                        dto,
                        null
                )
        );
    }

    @Override
    public void broadcastLeave(
            String roomId,
            ParticipantDto dto,
            String reason
    ) {
        messagingTemplate.convertAndSend(
                "/topic/rooms/" + roomId + "/participants",
                new ParticipantEvent(
                        ParticipantEventType.LEAVE,
                        roomId,
                        dto,
                        reason
                )
        );
        if (reason != null) {
            messagingTemplate.convertAndSendToUser(
                    dto.getUserId(),
                    "/queue/room-force-exit",
                    Map.of(
                            "roomId", roomId,
                            "reason", reason
                    )
            );
        }
    }

    @Override
    public void broadcastOwnerChanged(String roomId, String newOwnerId) {
        messagingTemplate.convertAndSend(
                "/topic/room/" + roomId + "/owner",
                new OwnerChangedEvent(roomId, newOwnerId)
        );

        log.info(
                "[OWNER_CHANGED] roomId={}, newOwnerId={}",
                roomId, newOwnerId
        );
    }

//    private void notifyRoomUsersChanged(String roomId) {
////        int current = repository.countByRoomIdAndIsActiveTrue(roomId);
//
//        messagingTemplate.convertAndSend(
//                "/topic/room-users/" + roomId,
//                "UPDATED"
//        );
////
////        messagingTemplate.convertAndSend(
////                "/topic/rooms/" + roomId + "/count",
////                Map.of("current", current)
////        );
//    }
//
//    private void sendCount(String roomId) {
//        int current = repository.countByRoomIdAndIsActiveTrue(roomId);
//        ChatRoomV2 room = roomV2Repository.findById(roomId).orElseThrow();
//
//        messagingTemplate.convertAndSend(
//                "/topic/rooms/" + roomId + "/count",
//                new RoomCountDto(current, room.getMaxParticipants())
//        );
//    }
}
