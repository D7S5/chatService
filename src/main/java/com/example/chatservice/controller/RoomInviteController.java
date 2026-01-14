package com.example.chatservice.controller;

import com.example.chatservice.dto.InviteCodeResponse;
import com.example.chatservice.dto.InviteJoinRequest;
import com.example.chatservice.dto.JoinByInviteResponse;
import com.example.chatservice.dto.RoomResponse;
import com.example.chatservice.entity.ChatRoomV2;
import com.example.chatservice.security.UserPrincipal;
import com.example.chatservice.service.ChatRoomV2Service;
import com.example.chatservice.service.RoomInviteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/rooms")
public class RoomInviteController {

    private final RoomInviteService inviteService;
    private final ChatRoomV2Service service;

    @PostMapping("/join-by-invite")
    public JoinByInviteResponse joinByInvite(
            @RequestBody InviteJoinRequest req,
            @AuthenticationPrincipal UserPrincipal user
            ) {
        return inviteService.joinByInvite(req.getInviteCode(), user.getId());
    }

    @PostMapping("/{roomId}/invite/reissue")
    public InviteCodeResponse reissueInvite(
            @PathVariable String roomId,
            @AuthenticationPrincipal UserPrincipal user
    ) throws AccessDeniedException {
        String inviteCode = inviteService.issueInviteCode(roomId, user.getId());

        return new InviteCodeResponse(inviteCode);
    }

    @GetMapping("/{roomId}")
    public RoomResponse getRoom(
            @PathVariable String roomId,
            @AuthenticationPrincipal UserPrincipal user
    ) {
        return service.getRoom(roomId, user.getId());
    }
}
