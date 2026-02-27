package com.example.chatService.kafka;

import com.example.chatService.dto.DMMessageKafkaDto;
import com.example.chatService.entity.DMMessage;
import com.example.chatService.entity.DMRoom;
import com.example.chatService.repository.DMRoomRepository;
import com.example.chatService.service.DMService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
public class DMBroadcastConsumer {

    private final SimpMessagingTemplate messagingTemplate;
    private final DMRoomRepository roomRepository;
    private final DMService dmService;

    @KafkaListener(
            topics = "dm-messages",
            groupId = "${chat.kafka.dm-consumer-group-id}", // 인스턴스별 고유
            containerFactory = "DMKafkaListenerContainerFactory"
    )
    public void broadcast(DMMessageKafkaDto dto) {

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

        String receiverId = dmService.getReceiverId(room.getRoomId(), dto.getSenderId());

        messagingTemplate.convertAndSendToUser(receiverId, "/queue/dm", message);
        messagingTemplate.convertAndSendToUser(dto.getSenderId(), "/queue/dm", message);
    }
}