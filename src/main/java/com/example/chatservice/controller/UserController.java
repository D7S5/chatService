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

        User user = userRepository.findById(userId).orElse(null);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "사용자를 찾을 수 없습니다."));
        }

        String nickname = request.nickname().trim();
        if (nickname.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "닉네임은 비어 있을 수 없습니다."));
        }

        if (userRepository.findByUsername(nickname).isPresent()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "이미 사용 중인 닉네임입니다."));
        }

        user.setUsername(nickname);
        user.setNicknameCompleted(true);
        userRepository.save(user);

        return ResponseEntity.ok(Map.of("nickname", nickname));
    }

}