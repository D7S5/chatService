package com.example.chatService.kafka;

import com.example.chatService.dto.DMMessageKafkaDto;
import com.example.chatService.entity.DMMessage;
import com.example.chatService.entity.DMRoom;
import com.example.chatService.repository.DMMessageRepository;
import com.example.chatService.repository.DMRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
public class DMStoreConsumer {

    private final DMRoomRepository roomRepository;
    private final DMMessageRepository messageRepository;

    @KafkaListener(
            topics = "dm-messages",
            groupId = "dm-store", // 공통 groupId
            containerFactory = "DMKafkaListenerContainerFactory"
    )
    public void store(DMMessageKafkaDto dto) {

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
    }
}