package com.example.chatService.service;


import com.example.chatService.component.ChatRateLimiter;
import com.example.chatService.dto.GroupMessageDto;
import com.example.chatService.kafka.GroupMessageProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class ChatMessageService {

    private final ChatRateLimiter chatRateLimiter;
    private final SimpMessagingTemplate messagingTemplate;
    private final GroupMessageProducer groupMessageProducer;

    public void handleSend(GroupMessageDto msg, String senderIdFromPrincipal) {

        msg.setSenderId(senderIdFromPrincipal);

        if (!chatRateLimiter.allowUser(senderIdFromPrincipal)) {
            return; // drop
        }

        if (!chatRateLimiter.allowRoom(msg.getRoomId())) {
            return; // drop
        }

        // ✅ 밴이면 알림 보내고 반드시 차단
        if (!chatRateLimiter.allowOrBan(senderIdFromPrincipal)) {
            long ttl = chatRateLimiter.getBanTtl(senderIdFromPrincipal);

            messagingTemplate.convertAndSendToUser(
                    senderIdFromPrincipal,
                    "/queue/rate-limit",
                    Map.of(
                            "type", "CHAT_BANNED",
                            "retryAfter", ttl
                    )
            );
            return;
        }

        groupMessageProducer.send(msg);
    }
}
