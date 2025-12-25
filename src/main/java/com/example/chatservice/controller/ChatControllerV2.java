package com.example.chatservice.controller;

import com.example.chatservice.component.ChatRateLimiter;
import com.example.chatservice.dto.*;
import com.example.chatservice.entity.ChatRoomV2;
import com.example.chatservice.kafka.GroupMessageProducer;
import com.example.chatservice.service.ChatRoomV2Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@Controller
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/rooms")
public class ChatControllerV2 {

    private final SimpMessagingTemplate messagingTemplate;

    private final ChatRoomV2Service chatRoomV2Service;
    private final GroupMessageProducer groupMessageProducer;

    private final ChatRateLimiter chatRateLimiter;
    @MessageMapping("/chat.send")
    public void send(GroupMessageDto msg) {

        if (!chatRateLimiter.allowUser(msg.getSenderId())) {
//            log.info("chatRateLimiter = {} ", msg.getSenderId());
            return ; // 조용히 drop
        }

        if (!chatRateLimiter.allowRoom(msg.getRoomId())) {
//            log.info("chatRateLimiter roomId = {} ", msg.getRoomId());
            return ;
        }
        // ❌ 제한 or 밴 상태
        if (!chatRateLimiter.allowOrBan(msg.getSenderId())) {

            long ttl = chatRateLimiter.getBanTtl(msg.getSenderId());

            // 🔥 본인에게만 제한 알림
            messagingTemplate.convertAndSendToUser(
                    msg.getSenderId(),
                    "/queue/rate-limit",
                    Map.of(
                            "type", "CHAT_BANNED",
                            "retryAfter", ttl
                    )
            );
//            return;
        }
        groupMessageProducer.send(msg);
    }

    @PostMapping
    public RoomResponse create(@RequestBody CreateRoomRequest request) {
        return chatRoomV2Service.createV2(request);
    }

    @GetMapping
    public ResponseEntity<List<ChatRoomV2>> getChatRooms() {
        try {
            List<ChatRoomV2> rooms = chatRoomV2Service.getAllRooms();
            return ResponseEntity.ok(rooms);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
