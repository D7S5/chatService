package com.example.chatService.service;

import com.example.chatService.entity.User;
import com.example.chatService.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final StringRedisTemplate redisTemplate;

    @Transactional
    public String setNickname(String userId, String nickname) {

        String trimmed = nickname.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("닉네임은 비어 있을 수 없습니다.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        if (userRepository.existsByUsername(trimmed)) {
            throw new IllegalStateException("이미 사용 중인 닉네임입니다.");
        }

        try {
            user.setUsername(trimmed);
            user.setNicknameCompleted(true);
            userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            // DB UNIQUE 제약에 걸린 경우
            throw new IllegalStateException("이미 사용 중인 닉네임입니다.");
        }
            // username cache 제거
            String key = "user:" + userId + ":username";
            redisTemplate.delete(key);

        return trimmed;
    }
}