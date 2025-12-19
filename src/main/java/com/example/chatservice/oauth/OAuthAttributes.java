package com.example.chatservice.oauth;

import com.example.chatservice.entity.AuthProvider;
import com.example.chatservice.entity.User;
import lombok.Builder;

import java.util.Map;

public class OAuthAttributes {

    private final Map<String, Object> attributes;
    private final String nameAttributeKey;

    private final String email;
    private final String username;
    private final String providerId;
    private final AuthProvider provider;

    @Builder
    public OAuthAttributes(
            Map<String, Object> attributes,
            String nameAttributeKey,
            String email,
            String username,
            String providerId,
            AuthProvider provider) {
        this.attributes = attributes;
        this.nameAttributeKey = nameAttributeKey;
        this.email = email;
        this.username = username;
        this.providerId = providerId;
        this.provider = provider;
    }

    public static OAuthAttributes of(String registrationId,
                                     String userNameAttributeName,
                                     Map<String, Object> attributes) {

        return switch (registrationId) {
            case "google" -> ofGoogle(userNameAttributeName, attributes);
            case "naver" -> ofNaver(userNameAttributeName, attributes);
            case "kakao" -> ofKakao(userNameAttributeName, attributes);
            default -> throw new IllegalArgumentException(
                    "Unsupported OAuth provider: " + registrationId);
        };
    }

    private static OAuthAttributes ofGoogle(
            String userNameAttributeName,
            Map<String, Object> attributes) {
        return OAuthAttributes.builder()
                .provider(AuthProvider.GOOGLE)
                .providerId((String) attributes.get("sub"))
                .email((String) attributes.get("email"))
                .username((String) attributes.get("name"))
                .attributes(attributes)
                .nameAttributeKey(userNameAttributeName)
                .build();
    }


    @SuppressWarnings("unchecked")
    private static OAuthAttributes ofNaver(
            String userNameAttributeName,
            Map<String, Object> attributes) {

        Map<String, Object> response =
                (Map<String, Object>) attributes.get("response");

        return OAuthAttributes.builder()
                .provider(AuthProvider.NAVER)
                .providerId((String) response.get("id"))
                .email((String) response.get("email"))
                .username((String) response.get("name"))
                .attributes(attributes)
                .nameAttributeKey(userNameAttributeName)
                .build();
    }

    private static OAuthAttributes ofKakao(
            String userNameAttributeName,
            Map<String, Object> attributes) {

        Map<String, Object> kakaoAccount =
                (Map<String, Object>) attributes.get("kakao_account");
        Map<String, Object> profile =
                (Map<String, Object>) kakaoAccount.get("profile");
        return OAuthAttributes.builder()
                .provider(AuthProvider.KAKAO)
                .providerId(String.valueOf(attributes.get("id")))
                .email((String) kakaoAccount.get("email"))
                .username((String) profile.get("nickname"))
                .attributes(attributes)
                .nameAttributeKey(userNameAttributeName)
                .build();
    }

    public User toEntity() {
        return User.builder()
                .email(email)
                .username(username)
                .provider(provider)
                .providerId(providerId)
                .role("USER")
                .nicknameCompleted(false)
                .build();
    }

    public String getEmail() { return email; }
    public String getUsername() { return username; }
    public AuthProvider getProvider() { return provider; }
    public String getProviderId() { return providerId; }
}
