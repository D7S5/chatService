package com.example.chatservice.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "jwt")
@Getter @Setter
public class JwtConfig {
    private String secret;

    private long accessTokenExpiry = 7_200_000;    // 2시간

    private long refreshTokenExpiry = 604_800_000; // 7일

    public String getSecret() {
        return secret != null ? secret.trim() : null;
    }
}