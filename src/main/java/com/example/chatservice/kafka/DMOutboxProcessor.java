package com.example.chatservice.kafka;

import com.example.chatservice.dto.DMMessageKafkaDto;
import com.example.chatservice.entity.DMOutbox;
import com.example.chatservice.repository.DMOutboxRepository;
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

    @Transactional
    @Scheduled(fixedDelay = 100)
    public void processOutbox() {

        List<DMOutbox> list = outboxRepository
                            .findTop100ByProcessedFalseOrderByIdAsc();

        if (list.isEmpty()) return;

        for (DMOutbox box : list) {

            try {
                DMMessageKafkaDto message = DMMessageKafkaDto.builder()
                        .roomId(box.getRoomId())
                        .senderId(box.getSenderId())
                        .content(box.getContent())
                        .sentAt(box.getEventTimestamp())
                        .build();

                kafkaTemplate.send(TOPIC, box.getRoomId() , message);

                box.setProcessed(true); // 멱등처리

            } catch (Exception e) {
                log.error("Outbox processing failed for id=" + box.getId(), e);
            }
        }
    }
}
