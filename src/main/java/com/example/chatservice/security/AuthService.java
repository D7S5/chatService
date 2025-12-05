package com.example.chatservice.security;

import com.example.chatservice.dto.*;
import com.example.chatservice.entity.User;
import com.example.chatservice.repository.RefreshTokenRepository;
import com.example.chatservice.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtConfig jwtConfig;
    private final RedisTemplate<String, String> redisTemplate;

    private final String REDIS_BLACKLIST_PREFIX = "blacklist:";

    public LoginResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String accessToken = jwtTokenProvider.generateAccessToken(authentication);
        String refreshToken = jwtTokenProvider.generateRefreshToken(request.email());

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        user.setRefreshToken(refreshToken);
        userRepository.save(user);

        LoginResponse res = new LoginResponse(
                accessToken,
                refreshToken,
                new UserDto(user.getId(), user.getUsername()));

        return res;
    }

    @Transactional
    public JwtResponse rotateRefreshToken(String oldRefreshToken) {

        User user = userRepository.findByRefreshToken(oldRefreshToken)
                .orElseThrow(() -> new IllegalArgumentException("Refresh Token Not Found"));

        if (Boolean.TRUE.equals(redisTemplate.hasKey(REDIS_BLACKLIST_PREFIX + oldRefreshToken)))
            throw new IllegalArgumentException("Refresh Token Reused (Blacklist");


        if (!jwtTokenProvider.validateToken(oldRefreshToken)) {
            throw new IllegalArgumentException("Refresh Token Expired");
        }

        String newAccessToken = jwtTokenProvider.generateAccessToken(user);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(user.getEmail());

        // 기존 Refresh Token 블랙리스트에 등록 (만료 시간까지)
        Claims claims = Jwts.parser()
                .setSigningKey(jwtTokenProvider.getKey()).build()
                .parseClaimsJws(oldRefreshToken)
                .getBody();
        long expireMillis = claims.getExpiration().getTime() - System.currentTimeMillis();
        redisTemplate.opsForValue().set(
                REDIS_BLACKLIST_PREFIX + oldRefreshToken,
                "BLACKLISTED",
                expireMillis,
                TimeUnit.MILLISECONDS
        );


        // 3) 기존 Refresh Token 폐기
        user.setRefreshToken(newRefreshToken);
        userRepository.save(user);

        return new JwtResponse(newAccessToken, newRefreshToken);
    }

    public void register(@Valid RegisterRequest request) {

        System.out.println("RegisterRequest: username=" + request.username() + ", email=" + request.email());
        if (userRepository.existsByUsername(request.username())) {
            throw new IllegalArgumentException("이미 존재하는 아이디입니다");
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다");
        }

        User user = new User();
        user.setUsername(request.username());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setEmail(request.email());
        user.setRole("USER");
        user.setOnline(false);
        System.out.println("User before save: username=" + user.getUsername() + ", email=" + user.getEmail() + ", role=" + user.getRole());
        userRepository.save(user);
    }
}