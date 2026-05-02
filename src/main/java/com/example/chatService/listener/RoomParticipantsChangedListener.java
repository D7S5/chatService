package com.example.chatService.listener;

import com.example.chatService.dto.RoomCountDto;
import com.example.chatService.event.RoomParticipantsChangedEvent;
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

    private final SimpMessagingTemplate messagingTemplate;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(RoomParticipantsChangedEvent event) {
        String roomId = event.getRoomId();

        messagingTemplate.convertAndSend(
                "/topic/room-users/" + roomId,
                "UPDATED"
        );

        messagingTemplate.convertAndSend(
                "/topic/rooms/" + roomId + "/count",
                new RoomCountDto(event.getCurrentCount(), event.getMaxParticipants())
        );
    }
}

