package com.example.chatservice.controller;

import com.example.chatservice.dto.*;
import com.example.chatservice.redis.OnlineStatusService;
import com.example.chatservice.redis.RoomUserCountService;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;

@RestController
@RequiredArgsConstructor
public class RedisController {

    private final OnlineStatusService onlineStatusService;
    private final RoomUserCountService roomUserCountService;
    private final SimpMessagingTemplate messagingTemplate;

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
