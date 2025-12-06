package com.example.chatservice.kafka;

import com.example.chatservice.dto.DirectMessageDto;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DMProducer {

    private final KafkaTemplate<String, DirectMessageDto> kafkaTemplate;
    private static final String TOPIC = "dm-messages";

    public void send(DirectMessageDto dto) {
        kafkaTemplate.send(TOPIC, dto.getRoomId(), dto);
    }
}
