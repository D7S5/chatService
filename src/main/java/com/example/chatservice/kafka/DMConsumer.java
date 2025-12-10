package com.example.chatservice.kafka;

import com.example.chatservice.dto.DMMessageKafkaDto;
import com.example.chatservice.entity.DMMessage;
import com.example.chatservice.entity.DMRoom;
import com.example.chatservice.repository.DMMessageRepository;
import com.example.chatservice.repository.DMRoomRepository;
import com.example.chatservice.service.DMService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
public class DMConsumer {

    private final SimpMessagingTemplate messagingTemplate;
    private final DMRoomRepository roomRepository;
    private final DMService dmService;

    private final DMMessageRepository messageRepository;
    @KafkaListener(
            topics = "dm-messages",
            groupId = "chat-service",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(DMMessageKafkaDto dto) {

        try {
            DMRoom room = roomRepository.findById(dto.getRoomId())
                    .orElseThrow(() -> new RuntimeException("Room not found"));

            OffsetDateTime sentAt =
                    OffsetDateTime.ofInstant(
                            Instant.ofEpochMilli(dto.getSentAt()),
                            ZoneId.of("Asia/Seoul")
                    );

            DMMessage message = DMMessage.builder()
                    .room(room)
                    .senderId(dto.getSenderId())
                    .content(dto.getContent())
                    .sentAt(sentAt)
                    .isRead(false)
                    .build();

            messageRepository.save(message);

            String receiverId = dmService.getReceiverId(room.getRoomId(), dto.getSenderId());

            messagingTemplate.convertAndSendToUser(receiverId, "/queue/dm", message);
            messagingTemplate.convertAndSendToUser(dto.getSenderId(), "/queue/dm", message);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}