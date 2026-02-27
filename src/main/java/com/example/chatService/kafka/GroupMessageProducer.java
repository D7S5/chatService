package com.example.chatService.kafka;

import com.example.chatService.dto.GroupMessageDto;
import com.example.chatService.dto.MessagingStatus;
import com.example.chatService.entity.GroupOutbox;
import com.example.chatService.repository.GroupMessageOutboxRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class GroupMessageProducer {

    private final GroupMessageOutboxRepository outboxRepository;

    public void send(GroupMessageDto dto) {

        GroupOutbox message = GroupOutbox.builder()
                        .roomId(dto.getRoomId())
                        .senderId(dto.getSenderId())
                        .senderName(dto.getSenderName())
                        .content(dto.getContent())
                        .eventTimestamp(dto.getSentAt())
                        .status(MessagingStatus.NEW)
                        .createAt(OffsetDateTime.now())
                        .build();

        outboxRepository.save(message);

    }
}
