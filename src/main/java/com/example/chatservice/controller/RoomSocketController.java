package com.example.chatservice.controller;

import com.example.chatservice.dto.RoomEnterDto;
import com.example.chatservice.service.ChatRoomV2Service;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class RoomSocketController {

    private final ChatRoomV2Service roomV2Service;

    @MessageMapping("/room.enter")
    public void enter(RoomEnterDto dto, SimpMessageHeaderAccessor accessor) {
        roomV2Service.enter(dto, accessor);
    }

    @MessageMapping("/room.leave")
    public void leave(RoomEnterDto dto, SimpMessageHeaderAccessor accessor) {
        roomV2Service.leaveBySession(dto.getRoomId(), accessor.getSessionId());
    }
}