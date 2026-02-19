package com.example.chatService.entity;

import com.example.chatService.oauth.OAuthAttributes;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class User implements UserDetails {
    @Id
    @Column(length = 36, nullable = false)
    @Builder.Default
    private String id = UUID.randomUUID().toString();

    @Column(nullable = true, unique = true)
    private String username;

    @Builder.Default
    @Column(nullable = false)
    private boolean nicknameCompleted = false;

    private String password;

    @Column(nullable = false, unique = true)
    private String email;

    @Builder.Default
    @Column(nullable = false)
    private boolean online = false;

    @Enumerated(EnumType.STRING)
    private AuthProvider provider;

    private String providerId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role;

    public User(String username, String password, String email, Role role) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.role = role;
    }

    public static User oauthUser(
            String email,
            String name,
            AuthProvider provider,
            String providerId
    ) {
        User user = new User();
        user.email = email;
        user.username = name; // 최초 닉네임 (나중에 변경 가능)
        user.password = null; // OAuth 전용
        user.role = Role.USER;
        user.provider = provider;
        user.providerId = providerId;
        return user;
    }
    public String getEmail() {
        return email;
    }

    public User updateOAuth(OAuthAttributes attributes) {
        this.username = attributes.getUsername();
        this.provider = attributes.getProvider();
        this.providerId = attributes.getProviderId();
        return this;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role));
    }
    @Override
    public boolean isAccountNonExpired() { return true; }
    @Override
    public boolean isAccountNonLocked() { return true; }
    @Override
    public boolean isCredentialsNonExpired() { return true; }
    @Override
    public boolean isEnabled() { return true; }
}