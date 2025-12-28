package com.example.chatservice.repository;

import com.example.chatservice.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email); // 이메일로 조회
    Optional<User> findById(String id);

    @Query("select u.username from User u where u.id = :userId")
    String findUsernameById(String userId);
}