package com.example.chatService.kafka;

import com.example.chatService.dto.GroupMessageDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class GroupMessageBroadcastConsumer {

    private final SimpMessagingTemplate messagingTemplate;

    private static final String TOPIC = "group-message-topic";

    @KafkaListener(
            topics = TOPIC,
            groupId = "${chat.kafka.consumer-group-id}", // 인스턴스별 고유
            containerFactory = "groupKafkaListenerContainerFactory"
    )
    public void broadcast(GroupMessageDto dto) {
        messagingTemplate.convertAndSend(
                "/topic/chat/" + dto.getRoomId(),
                dto
        );
    }
}