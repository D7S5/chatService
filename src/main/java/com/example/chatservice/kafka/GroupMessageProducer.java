package com.example.chatservice.kafka;

import com.example.chatservice.dto.GroupMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GroupMessageProducer {

    private final KafkaTemplate<String, GroupMessage> groupKafkaTemplate;
    private static final String TOPIC = "group-message-topic";

    public void send(String roomId, String senderId, String content) {

        GroupMessage message = GroupMessage.of(
                roomId,
                senderId,
                content
        );

        groupKafkaTemplate.send(
                TOPIC,
                roomId,  // partition key
                message
        );
    }
}
