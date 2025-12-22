package com.example.chatservice.kafka;

import com.example.chatservice.dto.GroupMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class GroupMessageConsumer {
    private final SimpMessagingTemplate messagingTemplate;

    private static final String TOPIC = "group-message-topic";

    @KafkaListener(
            topics = TOPIC,
            groupId = "group-chat-server"
    )
    public void consume(GroupMessage message) {

        messagingTemplate.convertAndSend(
                "/topic/chat/" + message.getRoomId(),
                message
        );
    }

}
