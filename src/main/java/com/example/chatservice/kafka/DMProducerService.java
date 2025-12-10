package com.example.chatservice.kafka;

import com.example.chatservice.dto.DMMessageKafkaDto;
import com.example.chatservice.entity.DMOutbox;
import com.example.chatservice.repository.DMOutboxRepository;
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
                    .processed(false)
                    .createAt(OffsetDateTime.now())
                    .build();

            outboxRepository.save(outbox);
        }
    }