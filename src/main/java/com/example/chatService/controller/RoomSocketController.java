package com.example.chatService.controller;

import com.example.chatService.dto.UserRoomSession;
import com.example.chatService.redis.UserSessionRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class RoomSocketController {
    private final UserSessionRegistry userSessionRegistry;

    @MessageMapping("/room.enter")
    public void enterRoom(UserRoomSession request,
                          @Header("simpSessionId") String sessionId) {
        userSessionRegistry.save(sessionId, request.getUserId(), request.getRoomId());
    }
}
