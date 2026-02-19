package com.example.chatService.controller;

import com.example.chatService.dto.NicknameRequest;
import com.example.chatService.security.JwtTokenProvider;
import com.example.chatService.security.UserPrincipal;
import com.example.chatService.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/set-nickname")
    public ResponseEntity<?> setNickname(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestBody NicknameRequest request) {

        String userId = userPrincipal.getId();

        try {
            String nickname = userService.setNickname(userId, request.nickname());
            return ResponseEntity.ok(Map.of("nickname", nickname));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", e.getMessage()));
        }
    }
}