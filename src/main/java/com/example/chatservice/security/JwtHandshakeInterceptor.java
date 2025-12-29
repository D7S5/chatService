package com.example.chatservice.security;

import com.example.chatservice.entity.User;
import com.example.chatservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

    private final JwtTokenProvider jwtProvider;
    private final UserRepository userRepository;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request,
                                   ServerHttpResponse response,
                                   WebSocketHandler wsHandler,
                                   Map<String, Object> attributes) {

        // SockJS → QueryParam에서 token 가져옴
        String uri = request.getURI().toString();

        String token = null;
        if (uri.contains("token=")) {
            token = uri.substring(uri.indexOf("token=") + 6);
        }

        if (token == null || !jwtProvider.validateToken(token)) {
            System.out.println("WebSocket JWT 없음 또는 유효하지 않음");
            return false;
        }

        String userId = jwtProvider.getSubject(token);
        User username = userRepository.findById(userId).orElse(null);

        // Principal 저장
        attributes.put("userId", userId);
        attributes.put("username", username);

        return true;
    }
    @Override
    public void afterHandshake(ServerHttpRequest request,
                               ServerHttpResponse response,
                               WebSocketHandler wsHandler,
                               Exception exception) {
    }
}

