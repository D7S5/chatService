package com.example.chatservice.controller;

import com.example.chatservice.dto.*;
import com.example.chatservice.redis.OnlineStatusService;
import com.example.chatservice.redis.RoomUserCountService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class RedisController {

    private final OnlineStatusService onlineStatusService;
    private final RoomUserCountService roomUserCountService;

    @MessageMapping("/user.enter")
    public void userEnter(UserEnterDto dto, SimpMessageHeaderAccessor accessor) {
        if (dto.getUserId() == null || dto.getUsername() == null) return;
        onlineStatusService.markOnline(dto);
    }
    @MessageMapping("/user.heartbeat")
    public void heartbeat(SimpMessageHeaderAccessor accessor) {
        String userId = (String) accessor.getSessionAttributes().get("userId");
        if ( userId != null) {
            onlineStatusService.refreshTTL(userId);
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
