package com.example.chatService.oauth;

import com.example.chatService.dto.UserDto;
import com.example.chatService.entity.User;
import com.example.chatService.repository.UserRepository;
import com.example.chatService.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
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

    @GetMapping("/me")
    public ResponseEntity<UserDto> me(Authentication authentication) {

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();

        User user = userRepository.findById(principal.getId())
                .orElseThrow();

        return ResponseEntity.ok(UserDto.from(user));
    }
}
