package com.example.chatservice.oauth;

import com.example.chatservice.entity.User;
import com.example.chatservice.repository.UserRepository;
import com.example.chatservice.security.CookieUtil;
import com.example.chatservice.security.JwtTokenProvider;
import com.example.chatservice.security.TokenHashUtil;
import com.example.chatservice.security.UserPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final CookieUtil cookieUtil;

    private final RedisTemplate<String, String> redisTemplate;

    @Value("${jwt.refresh-token-expiry}")
    private long jwtRefreshTokenExpiry;
    private static final String REDIS_CURRENT_PREFIX = "RT:current:";

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException {

        UserPrincipal principal =
                (UserPrincipal) authentication.getPrincipal();

        String userId = principal.getId();
        User user = userRepository.findById(userId).orElseThrow();


        // JWT 발급
        String accessToken = jwtTokenProvider.generateAccessToken(user);

        // refresh token 발급 + 쿠키 저장
        String refreshToken = jwtTokenProvider.generateRefreshToken(user);

        String hash = TokenHashUtil.hash(refreshToken);

        redisTemplate.opsForValue().set(
                REDIS_CURRENT_PREFIX + user.getId(),
                hash,
                jwtRefreshTokenExpiry,
                TimeUnit.MILLISECONDS
        );

        cookieUtil.addRefreshTokenCookie(response, refreshToken);

        String encoded = URLEncoder.encode(accessToken, StandardCharsets.UTF_8);

        String redirectUrl = !user.isNicknameCompleted()
                ? "http://localhost:3000/oauth/nickname?token=" + encoded
                : "http://localhost:3000/oauth/success?token=" + encoded;

        response.sendRedirect(redirectUrl);
    }
}
