package com.example.chatservice.oauth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j
public class OAuth2FailureHandler extends SimpleUrlAuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception
    ) throws IOException {

        log.error("🔥 OAuth2 Login Failed");
        log.error("Exception class: {}", exception.getClass().getName());
        log.error("Message: {}", exception.getMessage(), exception);

        response.sendError(
                HttpServletResponse.SC_UNAUTHORIZED,
                exception.getMessage()
        );
    }
}