package com.example.chatservice.repository;

import com.example.chatservice.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken , String> {
    Optional<RefreshToken> findByTokenAndRevokedFalse(String token);
}
