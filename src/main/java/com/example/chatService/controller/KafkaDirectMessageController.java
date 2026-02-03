package com.example.chatService.controller;

import com.example.chatService.dto.DMMessageKafkaDto;
import com.example.chatService.kafka.DMProducerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
@Slf4j
public class KafkaDirectMessageController {

    private final DMProducerService dmProducerService;

    @MessageMapping("/dm.send")
    public void send(DMMessageKafkaDto dto) {
//        System.out.println("timestamp : " + dto.getSentAt()); debug
        if ( dto.getSentAt() == 0L)
            dto.setSentAt(System.currentTimeMillis());

        dmProducerService.publish(dto);
    }
}