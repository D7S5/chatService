package com.example.chatservice.controller;

import com.example.chatservice.component.WebSocketEventListener;
import com.example.chatservice.dto.ChatRoomRequest;
import com.example.chatservice.entity.ChatRoom;
import com.example.chatservice.model.ChatMessage;
import com.example.chatservice.redis.OnlineStatusService;
import com.example.chatservice.service.ChatRoomService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class ChatController {
    private static final Logger log = LoggerFactory.getLogger(ChatController.class);

    @Autowired
    private SimpUserRegistry simpUserRegistry;

    @Autowired
    private ChatRoomService chatRoomService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat.sendMessage")
    public void sendMessage(ChatMessage chatMessage) {
        // /topic/room.{roomId}로 동적 전송
        messagingTemplate.convertAndSend("/topic/room." + chatMessage.getRoomId(), chatMessage);
    }

    @MessageMapping("/chat.addUser")
    public void addUser(ChatMessage chatMessage) {
        chatMessage.setContent(chatMessage.getSender() + "님이 입장했습니다.");
        messagingTemplate.convertAndSend("/topic/room." + chatMessage.getRoomId(), chatMessage);
    }

    @GetMapping("/rooms")
    public ResponseEntity<List<ChatRoom>> getChatRooms() {
        try {
            List<ChatRoom> rooms = chatRoomService.getAllRooms();
//            log.info("채팅방 목록 반환: count= {} ", rooms.size()); debug
            return ResponseEntity.ok(rooms);
        } catch (Exception e) {
//            log.error("채팅방 목록 조회 실패 {}", e.getMessage(), e); debug
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    @PostMapping("/rooms")
    public ResponseEntity<ChatRoom> createChatRoom(@RequestBody ChatRoomRequest request) {
        String roomName = request.getName();
        if (roomName == null || roomName.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(null);
        }
        ChatRoom newRoom = chatRoomService.createPublicRoom(roomName.trim());
        return ResponseEntity.ok(newRoom);
    }

    @GetMapping("/check-username")
    public ResponseEntity<Boolean> checkUsername(@RequestParam String username,
                                                 @AuthenticationPrincipal(expression = "username") String me) {
        log.info("닉네임 중복 확인: username={}", username);
        try {
            boolean isAvailable = simpUserRegistry.getUsers().stream()
                    .filter(name -> !name.equals(me))
                    .noneMatch(name -> name.equals(username));
            log.info("닉네임 사용 가능 여부: username={}, isAvailable={}", username, isAvailable);
            return ResponseEntity.ok(isAvailable);
        } catch (Exception e) {
            log.error("닉네임 중복 확인 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}