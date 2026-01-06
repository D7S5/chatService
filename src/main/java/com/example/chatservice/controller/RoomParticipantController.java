package com.example.chatservice.controller;

import com.example.chatservice.dto.ParticipantDto;
import com.example.chatservice.security.UserPrincipal;
import com.example.chatservice.service.RoomParticipantService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/rooms/{roomId}/participants")
public class RoomParticipantController {

    private final RoomParticipantService service;

    @PostMapping
    public ResponseEntity<Void> join(
            @PathVariable String roomId,
            @AuthenticationPrincipal UserPrincipal user
    ) {
        service.joinRoom(roomId, user.getId());
        service.broadcast(roomId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> leave(
            @PathVariable String roomId,
            @AuthenticationPrincipal UserPrincipal user
    ) {

        service.leaveRoom(roomId, user.getId());
        service.broadcast(roomId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public List<ParticipantDto> getParticipants(@PathVariable String roomId) {
        return service.getParticipants(roomId);
    }
}
