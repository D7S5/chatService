package com.example.chatservice.security;

import com.example.chatservice.dto.*;
import com.example.chatservice.entity.User;
import com.example.chatservice.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.SecretKey;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RedisTemplate<String, String> redisTemplate;

    private final CookieUtil cookieUtil;

    @Value("${jwt.refresh-token-expiry}")
    private long jwtRefreshTokenExpiry;
    private static final String REDIS_CURRENT_PREFIX = "RT:current:";
    private static final String REDIS_BLACKLIST_PREFIX = "RT:blacklist:";

    public LoginResponse login(LoginRequest request,
                               HttpServletResponse response) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();

        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        String accessToken = jwtTokenProvider.generateAccessToken(user);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user);

        String hash = TokenHashUtil.hash(refreshToken);

        redisTemplate.opsForValue().set(
                REDIS_CURRENT_PREFIX + user.getId(),
                hash,
                jwtRefreshTokenExpiry,
                TimeUnit.MILLISECONDS
        );

        cookieUtil.addRefreshTokenCookie(response, refreshToken);

        LoginResponse res = new LoginResponse(
                accessToken,
                UserDto.from(user));

        return res;
    }
    public JwtResponse reissue(String oldRefreshToken, HttpServletResponse response) {

        if ( oldRefreshToken == null || oldRefreshToken.isBlank() ) {
            throw new RuntimeException("Refresh Token missing");
        }

        if ( !jwtTokenProvider.validateToken(oldRefreshToken)) {
            throw new RuntimeException("invalid Refresh Token");
        }

        String userId = jwtTokenProvider.getSubject(oldRefreshToken);
        String oldHash = TokenHashUtil.hash(oldRefreshToken);

        // Reuse 감지
        if (redisTemplate.hasKey(REDIS_BLACKLIST_PREFIX + oldHash)) {
            redisTemplate.delete(REDIS_CURRENT_PREFIX + userId);
            cookieUtil.clearRefreshTokenCookie(response);
            throw new SecurityException("Refresh Token Reuse Detected");
        }

        String key = REDIS_CURRENT_PREFIX + userId;
        String savedHash = redisTemplate.opsForValue().get(key);
        if (!oldHash.equals(savedHash)) {
            redisTemplate.delete(key);
            cookieUtil.clearRefreshTokenCookie(response);
            throw new SecurityException("Refresh token Mismatch");
        }

        redisTemplate.opsForValue().set(
                REDIS_BLACKLIST_PREFIX + oldHash,
                "USED",
                remainingTTL(oldRefreshToken),
                TimeUnit.MILLISECONDS
        );

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String newAccessToken = jwtTokenProvider.generateAccessToken(user);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(user);
        String newHash = TokenHashUtil.hash(newRefreshToken);

        redisTemplate.opsForValue().set(
                REDIS_CURRENT_PREFIX + userId,
                newHash,
                jwtRefreshTokenExpiry,
                TimeUnit.MILLISECONDS
        );

        Cookie cookie = new Cookie("refreshToken", newRefreshToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(60 * 60 * 24 * 7);
        response.addCookie(cookie);

        return new JwtResponse(newAccessToken);
    }

    public void logout(HttpServletResponse response, Authentication authentication,
                       HttpServletRequest request) {
        String userId = null;

        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal principal) {
            userId = principal.getId();
//            System.out.println("authentication userId = " + userId);
        }

        if (userId == null) {
            userId = cookieUtil.tryResolveUserFromRefreshCookie(request);

            if (userId == null) {
                System.out.println("Cannot resolve user From refresh cookie");
                cookieUtil.clearRefreshTokenCookie(response);
                return;
            }
        }
        redisTemplate.delete(REDIS_CURRENT_PREFIX + userId);
        cookieUtil.clearRefreshTokenCookie(response);
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

    private long remainingTTL(String refreshToken) {
        Claims claims = Jwts.parser()
                .verifyWith(jwtTokenProvider.getKey())
                .build()
                .parseSignedClaims(refreshToken)
                .getPayload();

        long expirationTime = claims.getExpiration().getTime();
        long now = System.currentTimeMillis();

        return Math.max(expirationTime - now, 0);
    }
}

