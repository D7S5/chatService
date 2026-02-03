package com.example.chatService.controller;

import com.example.chatService.dto.FriendRequestSendDto;
import com.example.chatService.security.UserPrincipal;
import com.example.chatService.service.FriendService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user/friends")
@RequiredArgsConstructor
@Slf4j
public class FriendRequestController {

    private final FriendService friendService;

    @PostMapping("/request")
    public ResponseEntity<?> send(@RequestBody FriendRequestSendDto dto) {
        return ResponseEntity.ok(friendService.sendFriendRequest(dto.getFromUserId(), dto.getToUserId()));
    }

    @GetMapping("/received/{userId}")
    public ResponseEntity<?> getReceived(@PathVariable String userId) {
        return ResponseEntity.ok(friendService.getReceivedRequests(userId));
    }

    @PostMapping("/accept/{id}")
    public ResponseEntity<?> accept(@PathVariable Long id) {
        return ResponseEntity.ok(friendService.acceptFriendRequest(id));
    }

    @PostMapping("/reject/{id}")
    public ResponseEntity<?> reject(@PathVariable Long id) {
        return ResponseEntity.ok(friendService.reject(id));
    }

    @GetMapping("/list/{userId}")
    public ResponseEntity<?> friendList(@PathVariable String userId) {
        return ResponseEntity.ok(friendService.getFriendList(userId));
    }

    @DeleteMapping("/{friendUserId}")
    public ResponseEntity<?> removeFriend(
            @PathVariable String friendUserId,
            Authentication authentication
    ) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        friendService.removeFriend(principal.getId(), friendUserId);

        return ResponseEntity.ok().build();
    }
}
