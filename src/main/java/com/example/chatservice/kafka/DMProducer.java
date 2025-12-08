package com.example.chatservice.kafka;

import com.example.chatservice.dto.DMMessageKafkaDto;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

//@Service
//@RequiredArgsConstructor
//public class DMProducer {
//
//    private final KafkaTemplate<String, DMMessageKafkaDto> kafkaTemplate;
//    private static final String TOPIC = "dm-messages";
//
//    public void send(DMMessageKafkaDto dto) {
//        kafkaTemplate.send(TOPIC, dto.getRoomId(), dto);
//    }
//}
