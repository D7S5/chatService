package com.example.chatservice.kafka;

import com.example.chatservice.dto.GroupMessageDto;
import com.example.chatservice.entity.GroupOutbox;
import com.example.chatservice.repository.GroupMessageOutboxRepository;
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
public class GroupMessageOutboxProcessor {

    private final GroupMessageOutboxRepository outboxRepository;
    private final KafkaTemplate<String, GroupMessageDto> kafkaTemplate;
    private static final String TOPIC = "group-message-topic";

    @Transactional
    @Scheduled(fixedDelay = 100)
    public void processOutbox() {

        List<GroupOutbox> list = outboxRepository
                .findTop100ByProcessedFalseOrderByIdAsc();

        if (list.isEmpty()) return;

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

                box.setProcessed(true);

            } catch (Exception e) {
                log.error("GroupOutbox processing failed for id=" + box.getId(), e);
            }
        }
    }
}