package com.example.chatservice.controller;

import com.example.chatservice.redis.WsTokenService;
import com.example.chatservice.security.UserPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/ws")
public class WsTokenController {
    private final WsTokenService wsTokenService;

    public WsTokenController(WsTokenService wsTokenService) {
        this.wsTokenService = wsTokenService;
    }

    @PostMapping("/token")
    public ResponseEntity<?> createWsToken(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity.status(401).build();
        }
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        String userId = principal.getId();

        String token = wsTokenService.createTokenForUser(userId);
        return ResponseEntity.ok(Map.of("wsToken", token, "expiresIn", 120));
    }
}

