package com.example.chatservice.controller;

import com.example.chatservice.dto.RoomEnterDto;
import com.example.chatservice.service.ChatRoomV2Service;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class RoomSocketController {

    private final ChatRoomV2Service roomV2Service;

    @MessageMapping("/room.enter")
    public void enter(RoomEnterDto dto) {
        roomV2Service.enter(dto.getRoomId(), dto.getUserId(), dto.getUsername());
    }

    @MessageMapping("/room.leave")
    public void leave(RoomEnterDto dto) {
        roomV2Service.leave(dto.getRoomId(), dto.getUserId());
    }
}
