package com.example.chatService.controller;

import com.example.chatService.dto.WsTokenResponse;
import com.example.chatService.redis.WsTokenService;
import com.example.chatService.security.CookieUtil;
import com.example.chatService.security.JwtTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@AllArgsConstructor
@RequestMapping("/api/ws")
public class WsTokenController {
    private final WsTokenService wsTokenService;
    private final CookieUtil cookieUtil;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/token")
    public ResponseEntity<WsTokenResponse> createWsToken(HttpServletRequest req) {

        String refreshToken = cookieUtil.getRefreshToken(req);

        if (refreshToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new WsTokenResponse(null, 0));
        }

        if (!jwtTokenProvider.validateToken(refreshToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new WsTokenResponse(null, 0));
        }

        String userId = jwtTokenProvider.getSubject(refreshToken);
        String token = wsTokenService.createTokenForUser(userId);

        return ResponseEntity.ok(
                new WsTokenResponse(token, 120)
        );
    }
}