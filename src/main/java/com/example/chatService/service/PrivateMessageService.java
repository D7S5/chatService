package com.example.chatService.service;

import com.example.chatService.entity.PrivateMessage;
import com.example.chatService.repository.PrivateMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PrivateMessageService {

    private final PrivateMessageRepository repository;

    public PrivateMessage save(String sender, String receiver, String content) {
        PrivateMessage pm = PrivateMessage.builder()
                .sender(sender)
                .receiver(receiver)
                .content(content)
                .sentAt(OffsetDateTime.now())
                .readFlag(false)
                .build();
        return repository.save(pm);
    }
}
