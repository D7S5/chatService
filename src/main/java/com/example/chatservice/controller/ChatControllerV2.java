package com.example.chatservice.controller;

import com.example.chatservice.component.ChatRateLimiter;
import com.example.chatservice.dto.CreateRoomRequest;
import com.example.chatservice.dto.GroupMessageDto;
import com.example.chatservice.dto.RoomResponse;
import com.example.chatservice.entity.ChatRoomV2;
import com.example.chatservice.kafka.GroupMessageProducer;
import com.example.chatservice.service.ChatRoomV2Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.Response;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/rooms")
public class ChatControllerV2 {

    private final SimpMessagingTemplate messagingTemplate;

    private final StringRedisTemplate redis;


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
        if (!chatRateLimiter.allowOrBan(msg.getSenderId())) {

            long ttl = chatRateLimiter.getBanTtl(msg.getSenderId());

            // 본인에게 제한 알림
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

    @PostMapping("/create")
    public RoomResponse create(@RequestBody CreateRoomRequest request) {
        return chatRoomV2Service.createV2(request);
    }

    @GetMapping("/invite/{token}")
    public ResponseEntity<?> enterByInvite(@PathVariable String token) {

        String roomId = redis.opsForValue().get("room:invite:" + token);
        if (roomId == null) {
            return ResponseEntity.status(HttpStatus.GONE)
                    .body("초대 링크가 만료되었습니다.");
        }
        return ResponseEntity.ok(
                Map.of("roomId", roomId)
        );
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
