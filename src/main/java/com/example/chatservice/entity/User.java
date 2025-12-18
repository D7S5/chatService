package com.example.chatservice.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter @Setter
public class User implements UserDetails {
    @Id
    @Column(length = 36, nullable = false)
    private String id = UUID.randomUUID().toString();

    @Column(nullable = true, unique = true)
    private String username;

    @Column(nullable = true, unique = true)
    private String nickName;

    @Column(nullable = false)
    private boolean nicknameCompleted = false;

    private String password;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private boolean online = false;

    @Column(nullable = false)
    private String provider;

    @Column(nullable = false)
    private String providerId;

//    @Column(name = "refresh_token", length = 1000)
//    private String refreshToken;

    @Column(nullable = false)
    private String role;

    // 기본 생성자
    public User() {}

    public User(String username, String password, String email, String role) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.role = role;
    }

    public static User oauthUser(
            String email,
            String name,
            String provider,
            String providerId
    ) {
        User user = new User();
        user.email = email;
        user.username = name; // 최초 닉네임 (나중에 변경 가능)
        user.password = null; // OAuth 전용
        user.role = "USER";
        user.provider = provider;
        user.providerId = providerId;
        return user;
    }
    public String getEmail() {
        return email;
    }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

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