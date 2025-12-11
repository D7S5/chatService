package com.example.chatservice.security;

import com.example.chatservice.dto.*;
import com.example.chatservice.entity.User;
import com.example.chatservice.repository.RefreshTokenRepository;
import com.example.chatservice.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.*;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
    private final RedisTemplate<String, String> redisTemplate;

    private final CookieUtil cookieUtil;

    private final String REDIS_BLACKLIST_PREFIX = "blacklist:";

    public LoginResponse login(LoginRequest request,
                               HttpServletResponse response) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));


        String accessToken = jwtTokenProvider.generateAccessToken(authentication);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user);

        user.setRefreshToken(refreshToken);
        userRepository.save(user);

        cookieUtil.addRefreshTokenCookie(response, refreshToken);

        LoginResponse res = new LoginResponse(
                accessToken,
                new UserDto(user.getId(), user.getUsername()));

        return res;
    }

    @Transactional
    public JwtResponse rotateRefreshToken(String oldRefreshToken, HttpServletResponse response) {

        User user = userRepository.findByRefreshToken(oldRefreshToken)
                .orElseThrow(() -> new IllegalArgumentException("Refresh Token Not Found"));

        if (Boolean.TRUE.equals(redisTemplate.hasKey(REDIS_BLACKLIST_PREFIX + oldRefreshToken)))
            throw new IllegalArgumentException("Refresh Token Reused (Blacklist");

        if (!jwtTokenProvider.validateToken(oldRefreshToken)) {
            throw new IllegalArgumentException("Refresh Token Expired");
        }

        String newAccessToken = jwtTokenProvider.generateAccessToken(user);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(user);

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

        user.setRefreshToken(newRefreshToken);
        userRepository.save(user);

        cookieUtil.addRefreshTokenCookie(response, newRefreshToken);

        return new JwtResponse(newAccessToken);
    }

    public void logout(HttpServletResponse response, Authentication authentication,
                       HttpServletRequest request) {
        String email = null;

        if (authentication != null) {
            email = authentication.getName();
            System.out.println("authentication email = " + email);

        if (email == null) {
            System.out.println("authentication is null");
            email = cookieUtil.tryResolveUserFromRefreshCookie(request);

                if (email == null) {
                    System.out.println("Cannot resolve user From refresh cookie");
                    cookieUtil.clearRefreshTokenCookie(response);
                    return;
                }
            }
        }

        User saved = userRepository.findByEmail(email)
                        .orElseThrow(() -> new IllegalArgumentException("User not found"));

        saved.setRefreshToken(null);
        userRepository.save(saved);

        cookieUtil.clearRefreshTokenCookie(response);
    }

    public JwtResponse reissue(String oldRefreshToken, HttpServletResponse response) {

        if ( oldRefreshToken == null || oldRefreshToken.isBlank() ) {
            throw new RuntimeException("Refresh Token missing");
        }

        if ( !jwtTokenProvider.validateToken(oldRefreshToken)) {
            throw new RuntimeException("invalid Refresh Token");
        }

        String email = jwtTokenProvider.getSubject(oldRefreshToken);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getRefreshToken() == null ||
                !user.getRefreshToken().equals(oldRefreshToken)) {
            throw new RuntimeException("Refresh token mismatch");
        }

        String newAccessToken = jwtTokenProvider.generateAccessToken(user);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(user);

        user.setRefreshToken(newRefreshToken);
        userRepository.save(user);

        Cookie cookie = new Cookie("refreshToken", newRefreshToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(60 * 60 * 24 * 7);
        response.addCookie(cookie);

        return new JwtResponse(newAccessToken);
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