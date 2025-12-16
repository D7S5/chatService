package com.example.chatservice.controller;

import com.example.chatservice.dto.NicknameRequest;
import com.example.chatservice.entity.User;
import com.example.chatservice.repository.UserRepository;
import com.example.chatservice.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    @PostMapping("/set-nickname")
    public ResponseEntity<?> setNickname(@RequestHeader("Authorization") String authHeader,
                                         @RequestBody NicknameRequest request) {

        String token = authHeader.replace("Bearer ", "");
        if (!jwtTokenProvider.validateToken(token))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Invalid token");

        String email = jwtTokenProvider.getUserIdFromToken(token);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        if (userRepository.findByUsername(request.nickname()).isPresent())
            return ResponseEntity.badRequest().body(Map.of("message", "이미 사용 중인 닉네임입니다."));

        user.setUsername(request.nickname());
        user.setNicknameCompleted(true);
        userRepository.save(user);
        return ResponseEntity.ok(Map.of("nickname", user.getUsername()));
    }
    @GetMapping("/uuid")
    public ResponseEntity<?> getUUID(@RequestParam String username) {
        Optional<User> user = userRepository.findByUsername(username);
        if (user.isEmpty())
            return ResponseEntity.status(404).body("USER_NOT_FOUND");
        return ResponseEntity.ok(Map.of("userId", user.get().getId()));
    }
}