package com.example.chatService.kafka;

import com.example.chatService.dto.DMMessageKafkaDto;
import com.example.chatService.dto.MessagingStatus;
import com.example.chatService.entity.DMOutbox;
import com.example.chatService.repository.DMOutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${app.instance-id")
    private String workerId;

    @Transactional
    @Scheduled(fixedDelay = 50)
    public void processOutbox() throws Exception {

        int claimed = outboxRepository.claimBatch(workerId, BATCH_SIZE);
        if (claimed == 0) return;

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

                kafkaTemplate.send(TOPIC, box.getRoomId(), message).get();

                box.setStatus(MessagingStatus.SENT);
                box.setLockedBy(null);
                box.setLockedAt(null);

            } catch (Exception e) {
                // 실패시 다시 NEW
                box.setStatus(MessagingStatus.NEW);
                box.setLockedBy(null);
                box.setLockedAt(null);
            }
        }
    }
}