package com.example.chatService.kafka;

import com.example.chatService.dto.GroupMessageDto;
import com.example.chatService.entity.GroupMessageEntity;
import com.example.chatService.repository.GroupMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class GroupMessageStoreConsumer {

    private final GroupMessageRepository groupMessageRepository;

    private static final String TOPIC = "group-message-topic";

    @KafkaListener(
            topics = TOPIC,
            groupId = "group-chat-store", // 모든 인스턴스 동일 (분산 처리)
            containerFactory = "groupKafkaListenerContainerFactory"
    )
    public void store(GroupMessageDto dto) {
        GroupMessageEntity message = GroupMessageEntity.from(dto);
        groupMessageRepository.save(message);
    }
}
