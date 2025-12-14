package com.example.chatservice.repository;

import com.example.chatservice.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email); // 이메일로 조회
    Optional<User> findById(String id);

    boolean existsByUsernameIgnoreCase(String username);

    // RefreshToken 으로 사용자 찾기 (재발급 시 필요)
    Optional<User> findByRefreshToken(String refreshToken);
}