package com.example.chatservice.oauth;

import com.example.chatservice.entity.User;
import com.example.chatservice.repository.UserRepository;
import com.example.chatservice.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class CustomOAuth2UserService
        extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest request) {
        OAuth2User oAuth2User = super.loadUser(request);

        OAuthAttributes attributes =
                OAuthAttributes.of(
                        request.getClientRegistration().getRegistrationId(),
                        oAuth2User.getAttributes()
                );

        User user = userRepository.findByEmail(attributes.getEmail())
                .orElseGet(() -> {
                    User u = User.oauthUser(
                            attributes.getEmail(),
                            attributes.getName(),
                            attributes.getProvider(),
                            attributes.getProviderId()
                    );
                    u.setNicknameCompleted(false);
                    return userRepository.save(u);
                });

        return UserPrincipal.create(user, oAuth2User.getAttributes());
    }
}
