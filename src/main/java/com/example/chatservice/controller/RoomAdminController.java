package com.example.chatservice.controller;

import com.example.chatservice.dto.BanRequest;
import com.example.chatservice.dto.KickRequest;
import com.example.chatservice.security.UserPrincipal;
import com.example.chatservice.service.RoomParticipantService;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/rooms/{roomId}")
public class RoomAdminController {

    private final RoomParticipantService service;

    @PostMapping("/kick")
    public ResponseEntity<Void> kickUser(@PathVariable String roomId,
                                         @RequestBody KickRequest request,
                                         @AuthenticationPrincipal UserPrincipal me
                                         ) {
        service.kick(
                roomId,
                me.getId(),
                request.getTargetUserId()
        );
        return ResponseEntity.ok().build();
    }

    @PostMapping("/ban")
    public ResponseEntity<Void> banUser(
            @PathVariable String roomId,
            @RequestBody BanRequest request,
            @AuthenticationPrincipal UserPrincipal me
            ) {
        service.ban(
                roomId,
                me.getId(),
                request.getTargetUserId(),
                request.getReason()
        );
        return ResponseEntity.ok().build();
    }
}
