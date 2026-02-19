package com.example.chatService.controller;

import com.example.chatService.component.ChatRateLimiter;
import com.example.chatService.dto.CreateRoomRequest;
import com.example.chatService.dto.GroupMessageDto;
import com.example.chatService.dto.RoomResponse;
import com.example.chatService.dto.RoomType;
import com.example.chatService.entity.ChatRoomV2;
import com.example.chatService.kafka.GroupMessageProducer;
import com.example.chatService.security.UserPrincipal;
import com.example.chatService.service.ChatMessageService;
import com.example.chatService.service.ChatRoomService;
import com.example.chatService.service.RoomInviteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/rooms")
public class ChatControllerV2 {

    private final ChatMessageService chatMessageService;
    private final StringRedisTemplate redis;
    private final ChatRoomService chatRoomV2Service;
    private final RoomInviteService inviteService;


    @MessageMapping("/chat.send")
    public void send(GroupMessageDto msg,
                     Principal principal) {
        String senderId = principal.getName();
        chatMessageService.handleSend(msg, senderId);
    }

    @PostMapping("/create")
    public RoomResponse create(@RequestBody CreateRoomRequest request,
                               @AuthenticationPrincipal UserPrincipal user) {
        RoomResponse room = chatRoomV2Service.createV2(request, user.getId());

        if (room.getType() == RoomType.PRIVATE) {
            inviteService.joinByInvite(room.getInviteToken(), user.getId());
        }
        return room;
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