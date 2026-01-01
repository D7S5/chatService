package com.example.chatservice.controller;

import com.example.chatservice.dto.*;
import com.example.chatservice.redis.OnlineStatusService;
import com.example.chatservice.redis.OnlineStatusServiceV3;
import com.example.chatservice.redis.RoomUserCountService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class RedisController {

    private final OnlineStatusService onlineStatusService;
    private final RoomUserCountService roomUserCountService;

    @MessageMapping("/user.enter")
    public void userEnter(UserEnterDto dto, SimpMessageHeaderAccessor accessor) {
//        String sessionId = accessor.getSessionId();
//        System.out.println("Enter userId -> " + dto.getUserId() + " username => "  + dto.getUsername() + " sessionId => " + sessionId);
        if (dto.getUserId() == null || dto.getUsername() == null) return;
        onlineStatusService.markOnline(dto);
    }
    @MessageMapping("/user.heartbeat")
    public void heartbeat(Map<String, String> payload, SimpMessageHeaderAccessor accessor) {
        String sessionId = accessor.getSessionId();

        String userId = payload.get("userId");
        onlineStatusService.refreshTTL(sessionId);
    }

    @MessageMapping("/user.leave")
    public void leave(SimpMessageHeaderAccessor accessor) {

        String userId = (String) accessor.getSessionAttributes().get("userId");
        if ( userId != null) {
            onlineStatusService.markOffline(userId);
        }
    }

//    @MessageMapping("/room.enter")
//    public void enterRoom(RoomEnterDto dto) {
//        long count = roomUserCountService.increment(dto.getRoomId());
//
//        messagingTemplate.convertAndSend(
//                "/topic/room-count/" + dto.getRoomId(),
//                new RoomCountDto(dto.getRoomId(), count)
//        );
//    }
//
//    @MessageMapping("/room.leave")
//    public void leaveRoom(RoomLeaveDto dto) {
//        long count = roomUserCountService.decrement(dto.getRoomId());
//
//        messagingTemplate.convertAndSend(
//                "/topic/room-count/" + dto.getRoomId(),
//                new RoomCountDto(dto.getRoomId(), count)
//        );
//    }
}
