package com.example.chatService.controller;

import com.example.chatService.dto.ParticipantDto;
import com.example.chatService.security.UserPrincipal;
import com.example.chatService.service.ChatRoomService;
import com.example.chatService.service.RoomParticipantService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/rooms/")
public class RoomParticipantController {

    private final RoomParticipantService service;
    private final ChatRoomService chatRoomService;


    @PostMapping("/{roomId}/participants")
    public ResponseEntity<Void> join(
            @PathVariable String roomId,
            @AuthenticationPrincipal UserPrincipal user
    ) {
        chatRoomService.joinRoom(roomId, user.getId());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{roomId}/participants")
    public ResponseEntity<Void> leave(
            @PathVariable String roomId,
            @AuthenticationPrincipal UserPrincipal user
    ) {

        service.leaveRoom(roomId, user.getId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{roomId}/participants")
    public List<ParticipantDto> getParticipants(@PathVariable String roomId) {
        return service.getParticipants(roomId);
    }
    @GetMapping("/{roomId}/count")
    public Map<String, Integer> getRoomCount(@PathVariable String roomId) {
        return Map.of(
                "current", service.getCurrentCount(roomId));
    }
}
