package com.example.chatservice.listener;

import com.example.chatservice.dto.RoomCountDto;
import com.example.chatservice.event.RoomParticipantsChangedEvent;
import com.example.chatservice.repository.ChatRoomV2Repository;
import com.example.chatservice.service.RoomParticipantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@Slf4j
@RequiredArgsConstructor
public class RoomParticipantsChangedListener {

    private final RoomParticipantService roomParticipantService;
    private final SimpMessagingTemplate messagingTemplate;
    private final ChatRoomV2Repository roomRepository;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(RoomParticipantsChangedEvent event) {
        String roomId = event.getRoomId();

        int current = roomParticipantService.getCurrentCount(roomId);
        int max = roomRepository.findById(roomId)
                .orElseThrow()
                .getMaxParticipants();

        messagingTemplate.convertAndSend(
                "/topic/room-users/" + roomId,
                "UPDATED"
        );

        messagingTemplate.convertAndSend(
                "/topic/rooms/" + roomId + "/count",
                new RoomCountDto(current, max)
        );
    }
}

