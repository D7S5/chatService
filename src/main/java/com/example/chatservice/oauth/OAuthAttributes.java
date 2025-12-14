package com.example.chatservice.oauth;

import java.util.Map;

public class OAuthAttributes {

    private final String email;
    private final String name;
    private final String provider;
    private final String providerId;

    public OAuthAttributes(String email, String name,
                           String provider, String providerId) {
        this.email = email;
        this.name = name;
        this.provider = provider;
        this.providerId = providerId;
    }

    public static OAuthAttributes of(String registrationId,
                                     Map<String, Object> attributes) {

        if ("naver".equals(registrationId)) {
            return ofNaver(attributes);
        }
        return ofGoogle(attributes);
    }

    private static OAuthAttributes ofGoogle(Map<String, Object> attributes) {
        return new OAuthAttributes(
                (String) attributes.get("email"),
                (String) attributes.get("name"),
                "google",
                (String) attributes.get("sub")
        );
    }

    @SuppressWarnings("unchecked")
    private static OAuthAttributes ofNaver(Map<String, Object> attributes) {

        Map<String, Object> response =
                (Map<String, Object>) attributes.get("response");

        return new OAuthAttributes(
                (String) response.get("email"),
                (String) response.get("name"),
                "naver",
                (String) response.get("id")
        );
    }

    public String getEmail() { return email; }
    public String getName() { return name; }
    public String getProvider() { return provider; }
    public String getProviderId() { return providerId; }
}

