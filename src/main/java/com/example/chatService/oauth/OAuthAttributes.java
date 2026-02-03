package com.example.chatService.oauth;

import com.example.chatService.entity.AuthProvider;
import com.example.chatService.entity.Role;
import com.example.chatService.entity.User;
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

        String providerId = (String) attributes.get("sub");

        if ( providerId == null || providerId.isBlank()) {
            throw new IllegalArgumentException("Google providerId(Sub) is null");
        }

        String email = (String) attributes.get("email");

        String safeEmail = email != null
                ? email
                : AuthProvider.GOOGLE.name().toLowerCase() + "_" + providerId.substring(0, 8) + "@oauth.local";

        String username = (String) attributes.get("name");



        if ( username == null || username.isBlank()) {
            username = "google_" + providerId;
        }

        return OAuthAttributes.builder()
                .provider(AuthProvider.GOOGLE)
                .providerId(providerId)
                .email(safeEmail)
                .username(username)
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

        if (response == null) {
            throw new IllegalArgumentException("Naver response is null");
        }

        String providerId = (String) response.get("id");

        if ( providerId == null || providerId.isBlank()) {
            throw new IllegalArgumentException("Naver providerId(id) is null");
        }

        String email = (String) response.get("email");
        String safeEmail = email != null
                ? email
                : AuthProvider.NAVER.name().toLowerCase() + "_" + providerId.substring(0, 8) + "@oauth.local";

        String username = (String) response.get("name");

        if (username == null || username.isBlank()) {
            username = "naver_" + providerId;
        }

        return OAuthAttributes.builder()
                .provider(AuthProvider.NAVER)
                .providerId(providerId)
                .email(safeEmail)
                .username(username)
                .attributes(attributes)
                .nameAttributeKey(userNameAttributeName)
                .build();
    }

    @SuppressWarnings("unchecked")
    private static OAuthAttributes ofKakao(
            String userNameAttributeName,
            Map<String, Object> attributes) {

        Map<String, Object> kakaoAccount =
                (Map<String, Object>) attributes.get("kakao_account");

        if (kakaoAccount == null) {
            throw new IllegalArgumentException("Kakao account is null");
        }

        Map<String, Object> profile =
                (Map<String, Object>) kakaoAccount.get("profile");

        Object rawId = attributes.get("id");
        if ( rawId == null) {
            throw new IllegalArgumentException("Kakao providerId(id) is null");
        }
        String providerId = String.valueOf(rawId);

        String email = (String) kakaoAccount.get("email");

        String username = null;

        if (profile != null) {
            username = (String) profile.get("nickname");
        }

        if (username == null || username.isBlank()) {
            username = "kakao_" + providerId;
        }

        String safeEmail = email != null
                ? email
                : AuthProvider.KAKAO.name().toLowerCase() + "_" + providerId.substring(0, 8) + "@oauth.local";


        return OAuthAttributes.builder()
                .provider(AuthProvider.KAKAO)
                .providerId(providerId)
                .email(safeEmail)
                .username(username)
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
                .role(Role.USER)
                .nicknameCompleted(false)
                .build();
    }

    public String getEmail() { return email; }
    public String getUsername() { return username; }
    public AuthProvider getProvider() { return provider; }
    public String getProviderId() { return providerId; }
}
