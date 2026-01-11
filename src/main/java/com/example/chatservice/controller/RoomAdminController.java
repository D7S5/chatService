package com.example.chatservice.controller;

import com.example.chatservice.dto.AdminChangedResponse;
import com.example.chatservice.dto.AdminGrantRequest;
import com.example.chatservice.dto.BanRequest;
import com.example.chatservice.dto.KickRequest;
import com.example.chatservice.security.UserPrincipal;
import com.example.chatservice.service.RoomParticipantService;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
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