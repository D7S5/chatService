package com.example.chatService.kafka;

import com.example.chatService.dto.DMMessageKafkaDto;
import com.example.chatService.dto.MessagingStatus;
import com.example.chatService.entity.DMOutbox;
import com.example.chatService.repository.DMOutboxRepository;
import com.example.chatService.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class DMProducerService {

    private final DMOutboxRepository outboxRepository;
    private final UserRepository userRepository;

    @Transactional
    public void publish(DMMessageKafkaDto dto) {
        String senderName = dto.getSenderName();
        if (senderName == null || senderName.isBlank()) {
            senderName = userRepository.findUsernameById(dto.getSenderId());
        }

        if (senderName == null || senderName.isBlank()) {
            throw new IllegalArgumentException("Sender name is required for DM outbox messages.");
        }

        DMOutbox outbox = DMOutbox.builder()
                .roomId(dto.getRoomId())
                .senderId(dto.getSenderId())
                .senderName(senderName)
                .content(dto.getContent())
                .eventTimestamp(dto.getSentAt())
                .status(MessagingStatus.NEW)
                .createdAt(OffsetDateTime.now())
                .build();

        outboxRepository.save(outbox);
    }
}
