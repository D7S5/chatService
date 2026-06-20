package com.example.chatService.controller;

import com.example.chatService.dto.DMMessageKafkaDto;
import com.example.chatService.kafka.DMProducerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
@Slf4j
public class KafkaDirectMessageController {

    private final DMProducerService dmProducerService;

    @MessageMapping("/dm.send")
    public void send(DMMessageKafkaDto dto, Principal principal) {
        dto.setSenderId(principal.getName());
        if ( dto.getSentAt() == 0L)
            dto.setSentAt(System.currentTimeMillis());

        dmProducerService.publish(dto);
    }
}
