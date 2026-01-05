package com.example.chatservice.security;

import com.example.chatservice.entity.AuthProvider;
import com.example.chatservice.entity.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Getter
public class UserPrincipal implements UserDetails, OAuth2User {

    private final String id;
    private final String email;
    private final String username;
    private final String password;
    private final boolean nicknameCompleted;
    private final AuthProvider provider;
    private final String providerId;
    private final Collection<? extends GrantedAuthority> authorities;
    private Map<String, Object> attributes;

    private UserPrincipal(
            String id,
            String email,
            String username,
            String password,
            boolean nicknameCompleted,
            AuthProvider provider,
            String providerId,
            Collection<? extends GrantedAuthority> authorities
    ) {
        this.id = id;
        this.email = email;
        this.username = username;
        this.password = password;
        this.nicknameCompleted = nicknameCompleted;
        this.provider = provider;
        this.providerId = providerId;
        this.authorities = authorities;
    }

    public static UserPrincipal from(User user) {
        List<GrantedAuthority> authorities = Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_" + user.getRole())
        );
        return new UserPrincipal(
                user.getId(),
                user.getEmail(),
                user.getUsername(),
                user.getPassword(),
                user.isNicknameCompleted(),
                user.getProvider(),
                user.getProviderId(),
                authorities
        );
    }

    /* ================= JWT / 일반 로그인 ================= */

    public static UserPrincipal create(User user) {
        List<GrantedAuthority> authorities =
                List.of(new SimpleGrantedAuthority("ROLE_USER"));

        return new UserPrincipal(
                user.getId(),
                user.getEmail(),
                user.getUsername(),
                user.getPassword(),
                user.isNicknameCompleted(),
                user.getProvider(),
                user.getProviderId(),
                authorities
        );
    }

    /* ================= OAuth 로그인 ================= */

    public static UserPrincipal create(User user, Map<String, Object> attributes) {
        UserPrincipal principal = create(user);
        principal.attributes = attributes;
        return principal;
    }

    /* ================= OAuth2User ================= */

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public String getName() {
        return email != null && !email.isBlank()
            ? email
            : provider.name().toLowerCase() + "_" + providerId;
    }

//    @Override
//    public String getName() {
//        return id;
//    }

    public String getId() {
        return String.valueOf(id);
    }

    /* ================= UserDetails ================= */

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

}