package com.example.chatservice.oauth;

import com.example.chatservice.entity.User;
import com.example.chatservice.repository.UserRepository;
import com.example.chatservice.security.CookieUtil;
import com.example.chatservice.security.JwtTokenProvider;
import com.example.chatservice.security.UserPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final CookieUtil cookieUtil;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException {

        UserPrincipal principal =
                (UserPrincipal) authentication.getPrincipal();

        User user = userRepository.findByEmail(principal.getEmail()).orElseThrow();

        // JWT 발급
        String accessToken = jwtTokenProvider.generateAccessToken(user);

        // refresh token 발급 + 쿠키 저장
        String refreshToken = jwtTokenProvider.generateRefreshToken(user);
        user.setRefreshToken(refreshToken);
        userRepository.save(user);
        cookieUtil.addRefreshTokenCookie(response, refreshToken);

        // accessToken 프론트 전달 (쿼리스트링)

        String redirectUrl = !user.isNicknameCompleted()
                ? "http://localhost:3000/oauth/nickname?token=" + accessToken
                : "http://localhost:3000/oauth/success?token=" + accessToken;

        response.sendRedirect(redirectUrl);
    }
}
