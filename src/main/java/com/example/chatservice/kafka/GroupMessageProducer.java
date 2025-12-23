package com.example.chatservice.kafka;

import com.example.chatservice.dto.GroupMessageDto;
import com.example.chatservice.entity.GroupOutbox;
import com.example.chatservice.repository.GroupMessageOutboxRepository;
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
                        .processed(false)
                        .createAt(OffsetDateTime.now())
                        .build();

        outboxRepository.save(message);

    }
}
