package com.example.chatservice.controller;

import com.example.chatservice.dto.OnlineStatusDto;
import com.example.chatservice.dto.UserEnterDto;
import com.example.chatservice.redis.OnlineStatusService;
import lombok.AllArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;

@RestController
@AllArgsConstructor
public class RedisController {

    private OnlineStatusService onlineStatusService;
    @MessageMapping("/user.enter")
    public void userEnter(UserEnterDto dto) {
        if (dto.getUuid() == null || dto.getUsername() == null) return;
        onlineStatusService.markOnline(dto);
    }
    @MessageMapping("/user.heartbeat")
    public void heartbeat(Map<String, String> payload) {
        String uuid = payload.get("uuid");
        onlineStatusService.refreshTTL(uuid);
    }

    @MessageMapping("/user/leave")
    public void leave(Map<String, String> payload) {
        String uuid = payload.get("uuid");
        if ( uuid != null ) onlineStatusService.markOffline(uuid);
    }
}
