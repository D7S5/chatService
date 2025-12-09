package com.example.chatservice.kafka;

import com.example.chatservice.dto.DMMessageKafkaDto;
import com.example.chatservice.entity.DMOutbox;
import com.example.chatservice.repository.DMOutboxRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DMConsumerV2 {

    private final DMOutboxRepository outboxRepository;

    @KafkaListener(
            topics = "dm-message",
            groupId = "chat-service",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(DMMessageKafkaDto dto) {

        try {

            DMOutbox box = DMOutbox.builder()
                    .roomId(dto.getRoomId())
                    .senderId(dto.getSenderId())
                    .content(dto.getContent())
                    .eventTimestamp(dto.getTimestamp())
                    .processed(false)
                    .build();

            outboxRepository.save(box);
            System.out.println("Outbox stored: " + dto.getRoomId());

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
