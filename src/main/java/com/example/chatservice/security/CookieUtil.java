package com.example.chatservice.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CookieUtil {

    private static final String REFRESH_TOKEN_COOKIE = "refreshToken";
    private final JwtTokenProvider jwtTokenProvider;

    public void addRefreshTokenCookie(HttpServletResponse response, String refreshToken) {

        response.addHeader("Set-Cookie",
                REFRESH_TOKEN_COOKIE + refreshToken + "; " +
                        "Path=/; " +
                        "Max-Age=" + 60 * 60 * 24 * 14 + "; " + // 14일
                        "HttpOnly; " +
                        "Secure; " +
                        "SameSite=None");
    }

    public void clearRefreshTokenCookie(HttpServletResponse response) {

        response.addHeader("Set-Cookie",
                REFRESH_TOKEN_COOKIE + "=; " +
                        "Path=/; " +
                        "Max-Age=0; " +
                        "HttpOnly; " +
                        "Secure; " +
                        "SameSite=None");
    }

    public String getRefreshToken(HttpServletRequest request) {
        if (request.getCookies() == null) return null;

        for (Cookie cookie : request.getCookies()) {
            if (REFRESH_TOKEN_COOKIE.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    public String tryResolveUserFromRefreshCookie(HttpServletRequest request) {

        String refreshToken = getRefreshToken(request);
        if (refreshToken == null) return null;

        if (!jwtTokenProvider.validateToken(refreshToken)) {
            return null;
        }
        return jwtTokenProvider.getEmail(refreshToken);
    }

}
