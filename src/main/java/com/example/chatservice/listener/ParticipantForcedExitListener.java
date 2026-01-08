package com.example.chatservice.listener;

import com.example.chatservice.event.ParticipantForcedExitEvent;
import com.example.chatservice.service.RoomParticipantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class ParticipantForcedExitListener {
    private final SimpMessagingTemplate messagingTemplate;
    private final RoomParticipantService service;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(ParticipantForcedExitEvent event) {

        String roomId = event.getRoomId();
        String userId = event.getUserId();
        String reason = event.getReason();

        messagingTemplate.convertAndSendToUser(
                userId,
                "/queue/room-force-exit",
                Map.of(
                        "roomId", roomId,
                        "reason", reason
                )
        );
        log.info(
                "[FORCED_EXIT] roomId={}, userId={}, reason={}",
                roomId, userId, reason);
    }
}
