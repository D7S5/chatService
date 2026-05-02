package com.example.chatService.security;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class CookieUtilTest {

    private final JwtTokenProvider jwtTokenProvider = mock(JwtTokenProvider.class);
    private final CookieUtil cookieUtil = new CookieUtil(jwtTokenProvider);

    @Test
    void addRefreshTokenCookieWritesExpectedCookieHeader() {
        MockHttpServletResponse response = new MockHttpServletResponse();

        cookieUtil.addRefreshTokenCookie(response, "refresh-token");

        assertThat(response.getHeader("Set-Cookie"))
                .contains("refreshToken=refresh-token")
                .contains("Path=/")
                .contains("Max-Age=604800")
                .contains("HttpOnly")
                .contains("SameSite=Lax");
    }

    @Test
    void clearRefreshTokenCookieExpiresCookie() {
        MockHttpServletResponse response = new MockHttpServletResponse();

        cookieUtil.clearRefreshTokenCookie(response);

        assertThat(response.getHeader("Set-Cookie"))
                .contains("refreshToken=")
                .contains("Path=/")
                .contains("Max-Age=0")
                .contains("HttpOnly")
                .contains("SameSite=Lax");
    }

    @Test
    void getRefreshTokenReturnsNullWhenCookieIsMissing() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie("other", "value"));

        assertThat(cookieUtil.getRefreshToken(request)).isNull();
    }

    @Test
    void tryResolveUserFromRefreshCookieReturnsSubjectForValidToken() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie("refreshToken", "valid-token"));
        when(jwtTokenProvider.validateToken("valid-token")).thenReturn(true);
        when(jwtTokenProvider.getSubject("valid-token")).thenReturn("user-1");

        assertThat(cookieUtil.tryResolveUserFromRefreshCookie(request)).isEqualTo("user-1");

        verify(jwtTokenProvider).validateToken("valid-token");
        verify(jwtTokenProvider).getSubject("valid-token");
    }

    @Test
    void tryResolveUserFromRefreshCookieDoesNotValidateWhenCookieIsMissing() {
        MockHttpServletRequest request = new MockHttpServletRequest();

        assertThat(cookieUtil.tryResolveUserFromRefreshCookie(request)).isNull();
        verifyNoInteractions(jwtTokenProvider);
    }
}
