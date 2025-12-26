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

        accessor.getSessionAttributes().put("userId", dto.getUserId());
        accessor.getSessionAttributes().put("roomId", dto.getRoomId());

        roomV2Service.enter(dto.getRoomId(), dto.getUserId(), dto.getUsername());
    }

    @MessageMapping("/room.leave")
    public void leave(RoomEnterDto dto, SimpMessageHeaderAccessor accessor) {
        roomV2Service.leave(dto.getRoomId(), dto.getUserId());

        accessor.getSessionAttributes().remove("roomId");
    }
}
