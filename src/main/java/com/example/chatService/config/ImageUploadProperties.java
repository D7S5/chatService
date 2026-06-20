package com.example.chatService.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.upload")
public record ImageUploadProperties(
        String dir,
        String publicUrlPrefix,
        long maxImageSizeBytes
) {
}
