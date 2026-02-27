package com.example.chatService.kafka;

import com.example.chatService.dto.GroupMessageDto;
import com.example.chatService.dto.MessagingStatus;
import com.example.chatService.entity.GroupOutbox;
import com.example.chatService.repository.GroupMessageOutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class GroupMessageOutboxProcessor {

    private final GroupMessageOutboxRepository outboxRepository;
    private final KafkaTemplate<String, GroupMessageDto> kafkaTemplate;

    private static final int BATCH_SIZE = 100;
    private static final String TOPIC = "group-message-topic";

    // 인스턴스 고유 식별자
    private final String workerId = UUID.randomUUID().toString();
    @Transactional
    @Scheduled(fixedDelay = 50)
    public void processOutbox() {

        // 1) 먼저 선점 (다중 인스턴스 중복 방지 핵심)
        //  UPDATE SET status = PROCESSING
        int claimed = outboxRepository.claimBatch(workerId, BATCH_SIZE);
        if (claimed == 0) return;

        List<GroupOutbox> list = outboxRepository
                .findByStatusAndLockedByOrderByIdAsc(MessagingStatus.PROCESSING, workerId);

        for (GroupOutbox box : list) {
            try {
                GroupMessageDto message = GroupMessageDto.builder()
                        .roomId(box.getRoomId())
                        .senderId(box.getSenderId())
                        .senderName(box.getSenderName())
                        .sentAt(box.getEventTimestamp())
                        .content(box.getContent())
                        .build();

                kafkaTemplate.send(TOPIC, box.getRoomId(), message);

                box.setStatus(MessagingStatus.SENT);
                box.setLockedBy(null);
                box.setLockedAt(null);

            } catch (Exception e) {
                log.error("GroupOutbox processing failed for id=" + box.getId(), e);
            }
        }
    }
}