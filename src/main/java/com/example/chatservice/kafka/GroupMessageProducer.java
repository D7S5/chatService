package com.example.chatservice.kafka;

import com.example.chatservice.dto.GroupMessage;
import com.example.chatservice.dto.GroupMessageDto;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GroupMessageProducer {

    private final KafkaTemplate<String, GroupMessage> groupKafkaTemplate;
    private static final String TOPIC = "group-message-topic";

    public void send(GroupMessageDto dto) {

        GroupMessage message = new GroupMessage(
                dto.getRoomId(),
                dto.getSenderId(),
                dto.getSenderName(),
                dto.getContent(),
                System.currentTimeMillis()
        );

        groupKafkaTemplate.send(
                TOPIC,
                dto.getRoomId(),  // partition key
                message
        );
    }
}
