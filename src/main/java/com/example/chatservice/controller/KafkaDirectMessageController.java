package com.example.chatservice.controller;

import com.example.chatservice.dto.DMMessageKafkaDto;
import com.example.chatservice.kafka.DMProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
@Slf4j
public class KafkaDirectMessageController {

    private final DMProducer producer;
    private final KafkaTemplate<String, DMMessageKafkaDto> kafkaTemplate;

    @MessageMapping("/dm.send")
    public void sendMessage(DMMessageKafkaDto dto) {

        kafkaTemplate.send(
                "dm-messages",
                dto.getRoomId(),
                dto
        );
        log.info("DM message sent to Kafka: {}", dto); // debug
    }
}