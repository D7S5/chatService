package com.example.chatservice.controller;


import com.example.chatservice.dto.DirectMessageDto;
import com.example.chatservice.kafka.DMProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class KafkaDirectMessageController {

    private final DMProducer producer;

    @MessageMapping("/dm.send")
    public void sendMessage(DirectMessageDto dto) {
        dto.setTimestamp(System.currentTimeMillis());
        producer.send(dto);
    }
}
