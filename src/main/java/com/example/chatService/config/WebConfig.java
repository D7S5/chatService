package com.example.chatService.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final ImageUploadProperties imageUploadProperties;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String publicUrlPrefix = normalizePublicUrlPrefix(imageUploadProperties.publicUrlPrefix());
        Path uploadRoot = Path.of(imageUploadProperties.dir()).toAbsolutePath().normalize();

        registry.addResourceHandler(publicUrlPrefix + "/**")
                .addResourceLocations(uploadRoot.toUri().toString());
    }

    private String normalizePublicUrlPrefix(String publicUrlPrefix) {
        if (publicUrlPrefix == null || publicUrlPrefix.isBlank()) {
            return "/uploads";
        }

        return publicUrlPrefix.startsWith("/")
                ? publicUrlPrefix
                : "/" + publicUrlPrefix;
    }
}
