package com.example.chatservice.kafka;

import com.example.chatservice.dto.DirectMessageDto;
import com.example.chatservice.entity.Message;
import com.example.chatservice.entity.MessageEntity;
import com.example.chatservice.repository.KafkaMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DmConsumer {

    private final KafkaMessageRepository messageRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @KafkaListener(topics = "dm-messages", groupId = "chat-service")
    public void consume(DirectMessageDto dto) {
        MessageEntity saved = messageRepository.save(
                MessageEntity.builder()
                        .roomId(dto.getRoomId())
                        .senderId(dto.getSenderId())
                        .receiverId(dto.getReceiverId())
                        .content(dto.getContent())
                        .timestamp(dto.getTimestamp())
                        .build()
        );

        messagingTemplate.convertAndSendToUser(
                dto.getReceiverId(),
                "/queue/dm",
                saved
        );
        messagingTemplate.convertAndSendToUser(
                dto.getSenderId(),
                "/queue/dm",
                saved
        );
    }
}
