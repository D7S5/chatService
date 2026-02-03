package com.example.chatService.controller;

import com.example.chatService.dto.AdminChangedResponse;
import com.example.chatService.dto.AdminGrantRequest;
import com.example.chatService.dto.BanRequest;
import com.example.chatService.dto.KickRequest;
import com.example.chatService.security.UserPrincipal;
import com.example.chatService.service.RoomParticipantService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/rooms/{roomId}")
public class RoomAdminController {

    private final RoomParticipantService service;
    private final SimpMessagingTemplate messagingTemplate;

    @PostMapping("/kick")
    public ResponseEntity<Void> kickUser(@PathVariable String roomId,
                                         @RequestBody KickRequest request,
                                         @AuthenticationPrincipal UserPrincipal me
                                         ) {
        service.kick(
                roomId,
                request.getTargetUserId(),
                me.getId()
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
                request.getTargetUserId(),
                me.getId(),
                request.getReason()
        );
        return ResponseEntity.ok().build();
    }


    @MessageMapping("/rooms/{roomId}/admin")
    public void grantAdmin(
            @DestinationVariable String roomId,
            @Payload AdminGrantRequest request,
            Principal principal
    ) {
        String requesterId = principal.getName();

        AdminChangedResponse result =
                service.toggleAdmin(roomId, requesterId, request.getTargetUserId());

        messagingTemplate.convertAndSend(
                "/topic/rooms/" + roomId + "/participants",
                result
        );
    }
}