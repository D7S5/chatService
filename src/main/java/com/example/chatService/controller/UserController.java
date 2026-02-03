package com.example.chatService.controller;

import com.example.chatService.dto.NicknameRequest;
import com.example.chatService.security.JwtTokenProvider;
import com.example.chatService.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserService userService;

    @PostMapping("/set-nickname")
    public ResponseEntity<?> setNickname(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody NicknameRequest request) {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Authorization 헤더가 없습니다."));
        }

        String token = authHeader.substring(7);

        if (!jwtTokenProvider.validateToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "유효하지 않은 토큰입니다."));
        }

        String userId = jwtTokenProvider.getUserIdFromToken(token);

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