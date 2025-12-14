package com.example.chatservice.oauth;

import java.util.Map;

public class NaverOAuth2UserInfo {

    Map<String, Object> response;

    public NaverOAuth2UserInfo(Map<String, Object> attributes) {
        this.response = (Map<String, Object>) attributes.get("response");
    }

    public String getEmail() {
        return (String) response.get("email");
    }

    public String getName() {
        return (String) response.get("name");
    }

    public String getProviderId() {
        return (String) response.get("id");
    }
}
