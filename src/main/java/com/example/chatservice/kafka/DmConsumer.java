package com.example.chatservice.kafka;

import com.example.chatservice.dto.DMMessageKafkaDto;
import com.example.chatservice.entity.DMMessage;
import com.example.chatservice.entity.DMRoom;
import com.example.chatservice.repository.DMMessageRepository;
import com.example.chatservice.repository.DMRoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cglib.core.Local;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;


@Component
@RequiredArgsConstructor
@Slf4j
public class DmConsumer {

    private final DMRoomRepository roomRepository;
    private final DMMessageRepository messageRepository;

    @KafkaListener(
            topics = "dm-messages",
            groupId = "chat-service",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(DMMessageKafkaDto dto) {

        DMRoom room = roomRepository.findById(dto.getRoomId())
                .orElseThrow(() -> new RuntimeException("Room not found"));

        DMMessage message = DMMessage.builder()
                        .room(room)
                        .senderId(dto.getSenderId())
                        .content(dto.getContent())
                        .sentAt(LocalDateTime.now())
                        .isRead(false)
                        .build();

        room.setLastMessageTime(LocalDateTime.now());
        messageRepository.save(message);

        log.info("DM saved: room={}, sender={}", dto.getRoomId(), dto.getSenderId()); // debug
    }
}
