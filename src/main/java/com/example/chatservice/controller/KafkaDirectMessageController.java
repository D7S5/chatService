package com.example.chatservice.controller;

import com.example.chatservice.dto.DMMessageKafkaDto;
import com.example.chatservice.kafka.DMProducerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
@Slf4j
public class KafkaDirectMessageController {

    //    private final KafkaTemplate<String, DMMessageKafkaDto> kafkaTemplate;
    private static final String TOPIC = "dm-messages";
    private final DMProducerService dmProducerService;

//    @MessageMapping("/dm.send")
//    public void sendMessage(DMMessageKafkaDto dto) {
//
//        DMMessageKafkaDto kafkaDto = DMMessageKafkaDto.builder()
//                        .roomId(dto.getRoomId())
//                        .senderId(dto.getSenderId())
//                        .content(dto.getContent())
//                        .timestamp(System.currentTimeMillis())
//                        .build();
//
//        kafkaTemplate.send(
//                TOPIC,
//                kafkaDto.getRoomId(),
//                kafkaDto
//        );
//}
    @MessageMapping("/dm.send")
    public void send(DMMessageKafkaDto dto) {

        if( dto.getTimestamp() == 0L)
            dto.setTimestamp(System.currentTimeMillis());

        dmProducerService.publish(dto);
    }


}