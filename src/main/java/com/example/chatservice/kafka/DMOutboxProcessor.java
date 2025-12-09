package com.example.chatservice.kafka;

import com.example.chatservice.entity.DMMessage;
import com.example.chatservice.entity.DMOutbox;
import com.example.chatservice.entity.DMRoom;
import com.example.chatservice.repository.DMMessageRepository;
import com.example.chatservice.repository.DMOutboxRepository;
import com.example.chatservice.repository.DMRoomRepository;
import com.example.chatservice.service.DMService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

//log
import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.Level;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class DMOutboxProcessor {

    private final DMOutboxRepository outboxRepository;
    private final DMRoomRepository roomRepository;
    private final DMMessageRepository messageRepository;

    private final SimpMessagingTemplate messagingTemplate;
    private final DMService dmService;

    @Transactional
    @Scheduled(fixedDelay = 100)
    public void processOutbox() {

//        Logger sqlLogger = (Logger) LoggerFactory.getLogger("org.hibernate.SQL");
//        Logger bindLogger = (Logger) LoggerFactory.getLogger("org.hibernate.orm.jdbc.bind");
//
//        sqlLogger.setLevel(Level.OFF);
//        bindLogger.setLevel(Level.OFF);

        List<DMOutbox> list = outboxRepository
                            .findTop100ByProcessedFalseOrderByIdAsc();

        if (list.isEmpty()) return;

        for (DMOutbox box : list) {

            try {
                DMRoom room = roomRepository.findById(box.getRoomId())
                        .orElseThrow(() -> new RuntimeException("Room not found"));

                OffsetDateTime sentAt =
                        OffsetDateTime.ofInstant(
                                Instant.ofEpochMilli(box.getEventTimestamp()),
                                ZoneId.of("Asia/Seoul")
                        );

                DMMessage message = DMMessage.builder()
                        .room(room)
                        .senderId(box.getSenderId())
                        .content(box.getContent())
                        .sentAt(sentAt)
                        .isRead(false)
                        .build();

                room.setLastMessageTime(sentAt);
                messageRepository.save(message);

                box.setProcessed(true); // 멱등처리

                String receiverId = dmService.getReceiverId(box.getRoomId(), box.getSenderId());

                messagingTemplate.convertAndSendToUser(receiverId, "/queue/dm", message);
                messagingTemplate.convertAndSendToUser(box.getSenderId(), "/queue/dm", message);

            } catch (Exception e) {
                log.error("Outbox processing failed for id=" + box.getId(), e);
            }
        }
    }
}
