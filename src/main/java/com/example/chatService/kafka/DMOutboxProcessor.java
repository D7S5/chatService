package com.example.chatService.kafka;

import com.example.chatService.dto.DMMessageKafkaDto;
import com.example.chatService.dto.MessagingStatus;
import com.example.chatService.entity.DMOutbox;
import com.example.chatService.repository.DMOutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class DMOutboxProcessor {

    private final DMOutboxRepository outboxRepository;
    private final KafkaTemplate<String, DMMessageKafkaDto> kafkaTemplate;

    private static final String TOPIC = "dm-messages";
    private static final int BATCH_SIZE = 100;

    private final String workerId = java.util.UUID.randomUUID().toString();

    @Transactional
    @Scheduled(fixedDelay = 50)
    public void processOutbox() throws Exception {

        // 1) 선점
        int claimed = outboxRepository.claimBatch(workerId, BATCH_SIZE);
        if (claimed == 0) return;

        // 2) 내가 선점한 것만 가져오기
        List<DMOutbox> list = outboxRepository
                .findByStatusAndLockedByOrderByIdAsc(MessagingStatus.PROCESSING, workerId);

        for (DMOutbox box : list) {
            try {
                DMMessageKafkaDto message = DMMessageKafkaDto.builder()
                        .roomId(box.getRoomId())
                        .senderId(box.getSenderId())
                        .content(box.getContent())
                        .sentAt(box.getEventTimestamp())
                        .build();

                // 전송 성공 확인(포트폴리오용으로 명확)
                kafkaTemplate.send(TOPIC, box.getRoomId(), message).get();

                // 3) 성공 → SENT
                box.setStatus(MessagingStatus.SENT);
                box.setLockedBy(null);
                box.setLockedAt(null);

            } catch (Exception e) {
                log.error("DMOutbox processing failed for id={}", box.getId(), e);

                // 실패 → 다시 NEW로 풀어서 재시도 가능하게
                box.setStatus(MessagingStatus.NEW);
                box.setLockedBy(null);
                box.setLockedAt(null);
            }
        }
    }
}