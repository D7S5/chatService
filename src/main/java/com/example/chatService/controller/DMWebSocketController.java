package com.example.chatService.controller;

import com.example.chatService.entity.DMMessage;
import com.example.chatService.service.DMService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class DMWebSocketController {

    private final DMService dmService;
    private final SimpMessagingTemplate messagingTemplate;


    // DM V1
    @MessageMapping("/chat.private")
    public void sendPrivate(DMMessage payload, Principal principal) {

        String senderId = principal != null ? principal.getName() : payload.getSenderId();

        DMMessage msg = dmService.sendMessage(
                payload.getRoom().getRoomId(),
                senderId,
                payload.getContent()
        );

        String receiverId = dmService.getReceiverId(payload.getRoom().getRoomId(), senderId);

        // 상대방에게 보내기
        messagingTemplate.convertAndSendToUser(receiverId, "/queue/messages", msg);

        // 나에게도 보내기
        messagingTemplate.convertAndSendToUser(senderId, "/queue/messages", msg);
    }
}