package com.example.chatService.kafka;

import com.example.chatService.dto.DMMessageKafkaDto;
import com.example.chatService.dto.MessagingStatus;
import com.example.chatService.entity.DMOutbox;
import com.example.chatService.repository.DMOutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class DMProducerService {

    private final DMOutboxRepository outboxRepository;

    @Transactional
    public void publish(DMMessageKafkaDto dto) {

            DMOutbox outbox = DMOutbox.builder()
                    .roomId(dto.getRoomId())
                    .senderId(dto.getSenderId())
                    .content(dto.getContent())
                    .eventTimestamp(dto.getSentAt())
                    .status(MessagingStatus.NEW)
                    .createAt(OffsetDateTime.now())
                    .build();

            outboxRepository.save(outbox);
        }
    }