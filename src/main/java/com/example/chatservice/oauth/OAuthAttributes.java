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

        switch (registrationId) {
            case "google":
                return ofGoogle(attributes);
            case "naver":
                return ofNaver(attributes);
            case "kakao":
                return ofKakao(attributes);
            default:
                throw new IllegalArgumentException(
                        "Unsupported OAuth provider: " + registrationId);
        }
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

    private static OAuthAttributes ofKakao(Map<String, Object> attributes) {
        Map<String, Object> kakaoAccount =
                (Map<String, Object>) attributes.get("kakao_account");
        Map<String, Object> profile =
                (Map<String, Object>) kakaoAccount.get("profile");
        return new OAuthAttributes(
                (String) kakaoAccount.get("email"),
                (String) profile.get("nickname"),
                "kakao",
                String.valueOf(attributes.get("id"))
        );
    }

    public String getEmail() { return email; }
    public String getName() { return name; }
    public String getProvider() { return provider; }
    public String getProviderId() { return providerId; }
}
