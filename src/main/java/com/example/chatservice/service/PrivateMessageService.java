package com.example.chatservice.service;

import com.example.chatservice.entity.PrivateMessage;
import com.example.chatservice.repository.PrivateMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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
                .sentAt(LocalDateTime.now())
                .readFlag(false)
                .build();
        return repository.save(pm);
    }

    public List<PrivateMessage> getConversation(String userA, String userB) {
        List<PrivateMessage> aToB = repository.findBySenderAndReceiverOrderBySentAtAsc(userA, userB);
        List<PrivateMessage> bToA = repository.findBySenderAndReceiverOrderBySentAtAsc(userB, userA);

        List<PrivateMessage> all = new ArrayList<>();
        all.addAll(aToB);
        all.addAll(bToA);
        all.sort((m1, m2) -> m1.getSentAt().compareTo(m2.getSentAt()));
        return all;
    }
}
