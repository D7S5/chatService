package com.example.chatService.controller;

import com.example.chatService.dto.InviteCodeResponse;
import com.example.chatService.dto.InviteJoinRequest;
import com.example.chatService.dto.JoinByInviteResponse;
import com.example.chatService.dto.RoomResponse;
import com.example.chatService.security.UserPrincipal;
import com.example.chatService.service.ChatRoomService;
import com.example.chatService.service.RoomInviteService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/rooms")
public class RoomInviteController {

    private final RoomInviteService inviteService;
    private final ChatRoomService service;

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
        String inviteCode = inviteService.generateInviteCode(roomId, user.getId());

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
