package com.example.chatservice.oauth;

import com.example.chatservice.dto.UserDto;
import com.example.chatservice.entity.User;
import com.example.chatservice.repository.UserRepository;
import com.example.chatservice.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class OAuthApi {

    private final UserRepository userRepository;
    @PostMapping("/user/oauth/nickname")
    public ResponseEntity<UserDto> setNickname(
            @RequestBody Map<String, String> body,
            Authentication authentication
    ) {
        String nickname = body.get("nickname");

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        User user = userRepository.findById(principal.getId()).orElseThrow();

        user.setUsername(nickname);
        user.setNicknameCompleted(true);

        userRepository.save(user);

        return ResponseEntity.ok(UserDto.from(user));
    }

    @GetMapping("/user/nickname/check")
    public ResponseEntity<?> checkNickname(@RequestParam String nickname) {

        String trimmed = nickname.trim();

        if (trimmed.length() < 2 || trimmed.length() > 20) {
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "available", false,
                            "message", "닉네임은 2~20자 사이여야 합니다."
                    ));
        }

        boolean exists = userRepository.existsByUsernameIgnoreCase(trimmed);

        if (exists) {
            return ResponseEntity.ok(Map.of(
                    "available", false,
                    "message", "이미 사용 중인 닉네임입니다."
            ));
        }

        return ResponseEntity.ok(Map.of(
                "available", true,
                "message", "사용 가능한 닉네임입니다."
        ));
    }
}
