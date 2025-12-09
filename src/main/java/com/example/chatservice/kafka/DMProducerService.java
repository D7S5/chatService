package com.example.chatservice.kafka;

import com.example.chatservice.dto.DMMessageKafkaDto;
import com.example.chatservice.entity.DMOutbox;
import com.example.chatservice.repository.DMOutboxRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class DMProducerService {

    private final KafkaTemplate<String, DMMessageKafkaDto> kafkaTemplate;
    private final DMOutboxRepository outboxRepository;

    private static final String TOPIC = "dm-messages";

    @Transactional
    public void publish(DMMessageKafkaDto dto) {

        try {
            DMOutbox outbox = DMOutbox.builder()
                    .roomId(dto.getRoomId())
                    .senderId(dto.getSenderId())
                    .content(dto.getContent())
                    .eventTimestamp(dto.getTimestamp())
                    .processed(false)
                    .createAt(OffsetDateTime.now())
                    .build();

            outboxRepository.save(outbox);

            kafkaTemplate.send(TOPIC, dto.getRoomId(), dto);

//            log.info("DM Producer → Kafka 전송 완료: room={}, sender={}",
//                    dto.getRoomId(), dto.getSenderId()); //debug
        } catch (Exception e) {
            log.error("Producer failed");
            throw new RuntimeException("Kafka 발행 실패");
        }
    }
}
