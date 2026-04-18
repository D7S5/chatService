package com.example.chatService.controller;

import com.example.chatService.dto.*;
import com.example.chatService.redis.OnlineStatusService;
import com.example.chatService.redis.UserSessionRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class RedisController {

    private final OnlineStatusService onlineStatusService;
    private final UserSessionRegistry userSessionRegistry;

    @MessageMapping("/user.enter")
    public void userEnter(UserEnterDto dto) {
        if (dto.getUserId() == null || dto.getUsername() == null) return;
        onlineStatusService.markOnline(dto);
    }
    @MessageMapping("/user.heartbeat")
    public void heartbeat(SimpMessageHeaderAccessor accessor) {
        String userId = (String) accessor.getSessionAttributes().get("userId");
        String sessionId = accessor.getSessionId();

        if ( userId != null) {
            onlineStatusService.refreshTTL(userId);
        }
        if (sessionId != null) {
            userSessionRegistry.refreshTtl(sessionId);
        }
    }

    @MessageMapping("/user.leave")
    public void leave(SimpMessageHeaderAccessor accessor) {
        String userId = (String) accessor.getSessionAttributes().get("userId");
        if ( userId != null) {
            onlineStatusService.markOffline(userId);
        }
    }
}
