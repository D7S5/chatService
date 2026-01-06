package com.example.chatservice.service;

import com.example.chatservice.dto.OwnerChangedEvent;
import com.example.chatservice.dto.ParticipantDto;
import com.example.chatservice.dto.ParticipantEvent;
import com.example.chatservice.dto.ParticipantEventType;
import com.example.chatservice.entity.User;
import com.example.chatservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class ParticipantEventPublisherImpl implements ParticipantEventPublisher{

    private final SimpMessagingTemplate messagingTemplate;
    private final UserRepository userRepository;

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
        notifyRoomUsersChanged(roomId);
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
        notifyRoomUsersChanged(roomId);
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
//            User user = userRepository.findById(dto.getUserId())
//                    .orElseThrow();

//            System.out.println("reason != null , getEmail = " + user.getEmail());

            messagingTemplate.convertAndSendToUser(
                    dto.getUserId(),
                    "/queue/room-force-exit",
                    Map.of(
                            "roomId", roomId,
                            "reason", reason
                    )
            );
        }
        notifyRoomUsersChanged(roomId);
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

    private void publishParticipantEvent(
            String roomId,
            ParticipantEvent event
    ) {
        // 참가자 이벤트
        messagingTemplate.convertAndSend(
                "/topic/rooms/" + roomId + "/participants",
                event
        );

        // 🔥 프론트에서 REST 재조회 트리거
        notifyRoomUsersChanged(roomId);
    }


    private void notifyRoomUsersChanged(String roomId) {
        messagingTemplate.convertAndSend(
                "/topic/room-users/" + roomId,
                "UPDATED"
        );
    }
}
