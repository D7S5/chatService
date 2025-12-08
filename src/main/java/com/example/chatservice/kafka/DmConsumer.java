package com.example.chatservice.kafka;

import com.example.chatservice.converter.TimeConvert;
import com.example.chatservice.dto.DMMessageKafkaDto;
import com.example.chatservice.entity.DMMessage;
import com.example.chatservice.entity.DMMessageOutbox;
import com.example.chatservice.entity.DMRoom;
import com.example.chatservice.repository.DMMessageRepository;
import com.example.chatservice.repository.DMRoomRepository;
import com.example.chatservice.repository.OutboxRepository;
import com.example.chatservice.service.DMService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cglib.core.Local;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;


@Component
@RequiredArgsConstructor
@Slf4j
public class DmConsumer {

    private final DMRoomRepository roomRepository;
    private final DMMessageRepository messageRepository;
    private final DMService dmService;
    private final SimpMessagingTemplate messagingTemplate;

    @KafkaListener(
            topics = "dm-messages",
            groupId = "chat-service",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(DMMessageKafkaDto dto) {

        DMRoom room = roomRepository.findById(dto.getRoomId())
                .orElseThrow(() -> new RuntimeException("Room not found"));

        OffsetDateTime sentAt = TimeConvert.fromEpochMilliToKst(dto.getTimestamp());

        DMMessage message = DMMessage.builder()
                        .room(room)
                        .senderId(dto.getSenderId())
                        .content(dto.getContent())
                        .sentAt(sentAt)
                        .isRead(false)
                        .build();

        room.setLastMessageTime(sentAt);
        messageRepository.save(message);

        System.out.println("SentAt : " + message.getSentAt());
        System.out.println("LocalDateTime : " + LocalDateTime.now());
        System.out.println("OffsetDateTime : " + OffsetDateTime.now());

        String receiverId = dmService.getReceiverId(dto.getRoomId(), dto.getSenderId());

        messagingTemplate.convertAndSendToUser(receiverId, "/queue/dm", message);
        messagingTemplate.convertAndSendToUser(dto.getSenderId(), "/queue/dm", message);
    }
}
