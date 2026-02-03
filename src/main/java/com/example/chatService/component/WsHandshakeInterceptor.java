package com.example.chatService.component;

import com.example.chatService.redis.WsTokenService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class WsHandshakeInterceptor implements HandshakeInterceptor {

    private final WsTokenService wsTokenService;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request,
                                   ServerHttpResponse response,
                                   WebSocketHandler wsHandler,
                                   Map<String, Object> attributes) {

        if (!(request instanceof ServletServerHttpRequest servlet)) { return false; }

        HttpServletRequest req = servlet.getServletRequest();

        String wsToken = req.getParameter("ws-token");
        if (wsToken == null) { return false; }
        // 1회용 소비
        String userId = wsTokenService.consumeToken(wsToken);
        if (userId == null) { return false; }

        // Ws 세션에 Principal 정보를 저장
        attributes.put("userId", userId);
        return true;
    }
    @Override
    public void afterHandshake(ServerHttpRequest request,
                               ServerHttpResponse response,
                               WebSocketHandler wsHandler,
                               Exception exception) {}
}
